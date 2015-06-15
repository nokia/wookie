package wookie.sqlserver

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import java.nio.file.Paths
import scalaz._
import jodd.util.URLDecoder
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.hive.HiveContext
import scalaz.concurrent.Strategy
import scalaz.concurrent.Task
import java.util.Calendar
import java.text.SimpleDateFormat



case class ConnectionSpec(name: String, source: String, parameters: String, localStorage: Boolean, path: String) {
  val parametersMap: Map[String, String] = (for {
    param <- parameters.split("&&", -1) if param.split("==", -1).size == 2
  } yield {
    val k :: v :: Nil = param.split("==", -1).toList
    k -> v
  }).toMap
}


case class TableRegister(hiveContext: HiveContext) {
  
  implicit val scheduler = Strategy.DefaultTimeoutScheduler
  val conf = hiveContext.sparkContext.hadoopConfiguration
  var registry = Map[String, scalaz.stream.Process[Task, Unit]]()
 
  def setupTableRegistration(dir: String) = {

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
      val allChanges = (d.removed ++ d.added)
      if (!allChanges.isEmpty) {
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
  
  def startRefreshing(spec: ConnectionSpec) = synchronized {    
    if (spec.localStorage) {
      val f = DirectoryObserver.observeFilesRecursively(conf, spec.path, 1 second)(handleRefreshing(spec.path))
      registry = registry + (spec.name -> f)
      f.run.runAsync(f => ())
    }
  }
  
  def stopRefreshing(spec: ConnectionSpec) = synchronized {
    val removedProcess = for {
      proc <- registry.get(spec.name)
    } yield {
      val t = proc.kill.run.attemptRun
      proc
    }
    registry = registry - spec.name
    removedProcess
  }

  def addTable(path: String) = {

    val connection = for {
      spec <- decodeConnection(path)
    } yield spec
    
    val addingResult = for {
      spec <- connection
      df <- createDataFrame(path, spec)
      _ <- \/.fromTryCatchNonFatal(df.registerTempTable(spec.name))
    } yield spec
    
    println(s"Added: $connection : $addingResult")
    
    connection
  }

  def removeTable(path: String) = {
    
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
    } yield (spec)
  }
}