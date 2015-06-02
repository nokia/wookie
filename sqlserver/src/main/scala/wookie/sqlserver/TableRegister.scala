package wookie.sqlserver

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import java.nio.file.Paths
import scalaz._
import jodd.util.URLDecoder
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.hive.HiveContext
import scalaz.concurrent.Strategy

case class ConnectionSpec(name: String, source: String, parameters: String, localStorage: Boolean) {
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
 
  def setupTableRegistration(dir: String) = {

    val currentTables = DirectoryObserver.listDirectory(hiveContext.sparkContext.hadoopConfiguration, dir)

    handleRegistration(Some(Difference(currentTables, Set())))

    val observer = DirectoryObserver.observeDirectories(conf, dir, 1 second)(handleRegistration)
    observer.run.runAsync(f => ())
  }

  def handleRegistration: Option[Difference] => Unit = diff => {
    for {
      d <- diff
    } {
      d.added.foreach(addTable)
      d.removed.foreach(removeTable)
    }
  }
  
  def handleRefreshing(mainPath: String): Option[Difference] => Unit = diff => {
    for {
      d <- diff
    } {
      val refreshTable = (p: String) => { 
        removeTable(p) 
        addTable(p) 
      } 
      d.added.foreach(_ => refreshTable(mainPath))
      d.removed.foreach(_ => refreshTable(mainPath))
    }
  }
  
  def isLocalStorage(source: String): Boolean = source == "parquet" || source == "json" || source == "com.databricks.spark.csv"

  def decodeConnectionSpec(connectionStr: String): \/[Throwable, ConnectionSpec] = {
    connectionStr.split("__", -1).toList match {
      case name :: source :: Nil              => \/-(ConnectionSpec(name, source, "", isLocalStorage(source)))
      case name :: source :: conParams :: Nil => \/-(ConnectionSpec(name, source, conParams, isLocalStorage(source)))
      case _                                  => -\/(new RuntimeException("improper spec require spec|source"))
    }
  }
  
  def createDataFrame(path: String, conSpec: ConnectionSpec): \/[Throwable, DataFrame] = conSpec.source match {
    case "parquet" => 
      val registrationResult = \/.fromTryCatchNonFatal(hiveContext.load(path, conSpec.source))
      if (registrationResult.isLeft) {
        DirectoryObserver.observeFilesRecursively(conf, path, 1 second)(handleRefreshing(path)).run.runAsync(f => ())
      }
      registrationResult
    case "json" => \/.fromTryCatchNonFatal(hiveContext.load(path, conSpec.source))
    case "com.databricks.spark.csv" => \/.fromTryCatchNonFatal(hiveContext.load(conSpec.source, conSpec.parametersMap + ("path" -> path)))
    case _                  => \/.fromTryCatchNonFatal(hiveContext.load(conSpec.source, conSpec.parametersMap))
  }
  
  def registerDataFrame(name: String, df: DataFrame): \/[Throwable, Unit] = \/.fromTryCatchNonFatal(df.registerTempTable(name))

  def addTable(path: String): Unit = {
    val fileName = Paths.get(path).getFileName.toString

    val added = for {
      spec <- decodeConnection(path)
      df <- createDataFrame(path, spec)
      _ <- registerDataFrame(spec.name, df)
    } yield {
      (spec, path)
    }

    added.map { p => 
      if (p._1.localStorage) {
        \/.fromTryCatchNonFatal {
          DirectoryObserver.observeFilesRecursively(conf, path, 1 second)(handleRefreshing(path)).run.runAsync(f => ())
        }
      }
      p
    }
    println(s"Adding result: ${added}")
  }

  def removeTable(path: String): Unit = {
    val removed = for {
      spec <- decodeConnection(path)
      _ <- \/.fromTryCatchNonFatal {
        hiveContext.dropTempTable(spec.name)
      }
    } yield {
      (spec, path)
    }
    
    println(s"Removing result: ${removed}")
  }  
  
  
  private def decodeConnection(path: String) = {  
    val fileName = Paths.get(path).getFileName.toString

    for {
      connectionStr <- \/.fromTryCatchNonFatal(URLDecoder.decode(fileName))
      spec <- decodeConnectionSpec(connectionStr)
    } yield (spec)
  }
}