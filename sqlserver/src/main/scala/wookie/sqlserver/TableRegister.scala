/*
 * Copyright (C) 2014-2015 by Nokia.
 * See the LICENCE.txt file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package wookie.sqlserver

import org.apache.spark.Logging

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import java.nio.file.Paths
import scalaz._
import jodd.util.URLDecoder
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.hive.HiveContext
import scalaz.concurrent.Strategy
import scalaz.concurrent.Task

case class ConnectionSpec(name: String, source: String, parameters: String, localStorage: Boolean, path: String) {
  val parametersMap: Map[String, String] = (for {
    param <- parameters.split("&&", -1) if param.split("==", -1).size == 2
  } yield {
    val k :: v :: Nil = param.split("==", -1).toList
    k -> v
  }).toMap
}


case class TableRegister(hiveContext: HiveContext) extends Logging {

  implicit val scheduler = Strategy.DefaultTimeoutScheduler
  val conf = hiveContext.sparkContext.hadoopConfiguration
  var registry = Map[String, scalaz.stream.Process[Task, Unit]]()

  def setupTableRegistration(dir: String): Unit = {

    val currentTables = DirectoryObserver.listDirectory(hiveContext.sparkContext.hadoopConfiguration, dir)

    handleRegistration(Some(Difference(currentTables, Set())))

    val observer = DirectoryObserver.observeDirectories(conf, dir, 1 second)(handleRegistration)
    observer.run.runAsync(f => ())
  }

  def handleRegistration: Option[Difference] => Unit = diff => {
    for (d <- diff) {
      d.removed.foreach { p => for {
          spec <- removeTable(p)
        } yield stopRefreshing(spec)
      }
      d.added.foreach { p => for {
          spec <- addTable(p)
        } yield startRefreshing(spec)
      }
    }
  }

  def handleRefreshing(mainPath: String): Option[Difference] => Unit = diff => {
    for (d <- diff) {
      val allChanges = d.removed ++ d.added
      if (allChanges.nonEmpty) {
        removeTable(mainPath)
        addTable(mainPath)
      }
    }
  }

  def isLocalStorage(source: String): Boolean = source == "parquet" || source == "json" || source == "com.databricks.spark.csv"

  def decodeConnectionSpec(connectionStr: String, mainPath: String): \/[Throwable, ConnectionSpec] = {
    connectionStr.split("__", -1).toList match {
      case name :: source :: Nil              => \/-(ConnectionSpec(name, source, "", isLocalStorage(source), mainPath))
      case name :: source :: conParams :: Nil => \/-(ConnectionSpec(name, source, conParams, isLocalStorage(source), mainPath))
      case _                                  => -\/(new RuntimeException("improper spec require spec|source"))
    }
  }

  def createDataFrame(path: String, conSpec: ConnectionSpec): \/[Throwable, DataFrame] = conSpec.source match {
    case "parquet" | "json" => \/.fromTryCatchNonFatal(hiveContext.read.format(conSpec.source).load(path))
    case "com.databricks.spark.csv" => \/.fromTryCatchNonFatal(hiveContext.read.format(conSpec.source).options(conSpec.parametersMap + ("path" -> path)).load)
    case _                  => \/.fromTryCatchNonFatal(hiveContext.read.format(conSpec.source).options(conSpec.parametersMap).load)
  }

  def startRefreshing(spec: ConnectionSpec): Unit = synchronized {
    if (spec.localStorage) {
      val f = DirectoryObserver.observeFilesRecursively(conf, spec.path, 1 second)(handleRefreshing(spec.path))
      registry = registry + (spec.name -> f)
      f.run.runAsync(f => ())
    }
  }

  def stopRefreshing(spec: ConnectionSpec): Option[scalaz.stream.Process[Task, Unit]] = synchronized {
    val removedProcess = for {
      proc <- registry.get(spec.name)
    } yield {
      val t = proc.kill.run.attemptRun
      proc
    }
    registry = registry - spec.name
    removedProcess
  }

  def addTable(path: String): Throwable \/ ConnectionSpec = {

    val connection = for {
      spec <- decodeConnection(path)
    } yield spec

    val addingResult = for {
      spec <- connection
      df <- createDataFrame(path, spec)
      _ <- \/.fromTryCatchNonFatal(df.registerTempTable(spec.name))
    } yield spec

    logInfo(s"Added: $connection : $addingResult")

    connection
  }

  def removeTable(path: String): Throwable \/ ConnectionSpec = {

    val connection = for {
      spec <- decodeConnection(path)
    } yield spec

    for {
      spec <- connection
      _ <- \/.fromTryCatchNonFatal(hiveContext.dropTempTable(spec.name))
    } yield spec

    connection
  }


  private def decodeConnection(path: String) = {
    val fileName = Paths.get(path).getFileName.toString

    for {
      connectionStr <- \/.fromTryCatchNonFatal(URLDecoder.decode(fileName))
      spec <- decodeConnectionSpec(connectionStr, path)
    } yield spec
  }
}
