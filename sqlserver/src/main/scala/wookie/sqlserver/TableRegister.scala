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

import java.nio.file.Paths
import java.util.concurrent.{Executors, ScheduledExecutorService}

import jodd.util.URLDecoder
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.log4s._

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scalaz._
import scalaz.concurrent.{Strategy, Task}

case class ConnectionSpec(name: String, source: String, parameters: String, localStorage: Boolean, path: String) {
  val parametersMap: Map[String, String] = (for {
    param <- parameters.split("&&", -1) if param.split("==", -1).length == 2
  } yield {
    val k :: v :: Nil = param.split("==", -1).toList
    k -> v
  }).toMap
}


case class TableRegister(session: SparkSession) {

  //implicit val scheduler = Strategy.DefaultTimeoutScheduler
  private[this] val log = getLogger
  val conf = session.sparkContext.hadoopConfiguration
  var registry = Map[String, scalaz.stream.Process[Task, Unit]]()

  def setupTableRegistration(paths: Map[String, String]): Unit = {
    implicit val scheduler = Executors.newScheduledThreadPool(Math.min(Runtime.getRuntime.availableProcessors, paths.size),
      Strategy.DefaultDaemonThreadFactory)
    paths.foreach {
      case (tableName, path) =>
        log.info(s"Registering $tableName with $path")
        val spec = ConnectionSpec(tableName, "parquet", "", localStorage = true, path)
        val df = registerDataFrame(path, spec)
        log.info(s"Result of registering $df")
        startRefreshing(spec)
    }
  }

  def setupTableRegistration(dir: String): Unit = {

    implicit val scheduler = Strategy.DefaultTimeoutScheduler
    val currentTables = DirectoryObserver.listDirectory(session.sparkContext.hadoopConfiguration, dir)

    handleRegistration(scheduler)(Some(Difference(currentTables, Set())))

    val observer = DirectoryObserver.observeDirectories(conf, dir, 1 minute)(handleRegistration(scheduler))
    observer.run.unsafePerformAsync(f => ())
  }

  def handleRegistration(implicit scheduler: ScheduledExecutorService): Option[Difference] => Unit = diff => {
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
    log.info(s"Refreshing path : $mainPath")
    for (d <- diff) {
      val allChanges = d.removed ++ d.added
      if (allChanges.nonEmpty) {
        removeTable(mainPath)
        addTable(mainPath)
      }
    }
  }

  def isLocalStorage(source: String): Boolean = source == "parquet" || source == "json" || source == "csv"

  def decodeConnectionSpec(connectionStr: String, mainPath: String): \/[Throwable, ConnectionSpec] = {
    connectionStr.split("__", -1).toList match {
      case name :: source :: Nil              => \/-(ConnectionSpec(name, source, "", isLocalStorage(source), mainPath))
      case name :: source :: conParams :: Nil => \/-(ConnectionSpec(name, source, conParams, isLocalStorage(source), mainPath))
      case _                                  => -\/(new RuntimeException("improper spec require spec|source"))
    }
  }

  def registerDataFrame(path: String, conSpec: ConnectionSpec): \/[Throwable, DataFrame] = conSpec.source match {
    case "parquet" | "json" | "csv" => \/.fromTryCatchNonFatal {
      session.catalog.createTable(conSpec.name, path, conSpec.source)
    }
    case _                  => \/.fromTryCatchNonFatal {
      session.catalog.createTable(conSpec.name, conSpec.source, conSpec.parametersMap)
    }
  }

  def startRefreshing(spec: ConnectionSpec)(implicit scheduler: ScheduledExecutorService): Unit = synchronized {
    log.info(s"Starting refreshing: $spec")
    if (spec.localStorage) {
      val f = DirectoryObserver.observeFilesRecursively(conf, spec.path, 1 minute)(handleRefreshing(spec.path))
      registry = registry + (spec.name -> f)
      f.run.unsafePerformAsync(f => ())
    }
  }

  def stopRefreshing(spec: ConnectionSpec): Option[scalaz.stream.Process[Task, Unit]] = synchronized {
    val removedProcess = for {
      proc <- registry.get(spec.name)
    } yield {
      proc.kill.run.unsafePerformSyncAttempt
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
      df <- registerDataFrame(path, spec)
    } yield spec

    log.info(s"Added: $connection : $addingResult")

    connection
  }

  def removeTable(path: String): Throwable \/ ConnectionSpec = {

    val connection = for {
      spec <- decodeConnection(path)
    } yield spec

    for {
      spec <- connection
      _ <- \/.fromTryCatchNonFatal(session.catalog.dropTempView(spec.name))
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
