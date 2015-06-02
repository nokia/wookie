package wookie.sqlserver

import org.apache.spark.scheduler.StatsReportListener
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.sql.hive.thriftserver.HiveThriftServer2
import org.rogach.scallop.ScallopConf

import wookie.spark.SparkApp
import wookie.spark.cli.Input
import wookie.spark.cli.Name

trait SparkSQLServerConf extends Input with Name { self: ScallopConf =>
  lazy val hiveconfs = opt[List[String]]("hiveconf", descr = "hive configs").map {
    f =>
      val tuples = f.map {
        x =>
          val splitted = x.split("=", -1)
          if (splitted.size == 2) {
            Some((splitted(0), splitted(1)))
          } else {
            None
          }
      }
      tuples.filter(_ != None).map(_.get).toMap
  }
}

object SparkSQLServer extends SparkApp[SparkSQLServerConf](new ScallopConf(_) with SparkSQLServerConf) {

  protected var hiveContext: HiveContext = _

  def run(opt: SparkSQLServerConf): Unit = {
    processCliParams(opt.hiveconfs.get.getOrElse(Map()))
    setDefaultParams()

    sc.addSparkListener(new StatsReportListener())
    hiveContext = new HiveContext(sc)

    TableRegister(hiveContext).setupTableRegistration(opt.inputURL())

    println("Tables registered proceeding with launch...")

    HiveThriftServer2.startWithContext(hiveContext)
  }

  def processCliParams(arg: Map[String, String]) = {
    for ((k, v) <- arg) (System.setProperty(k, v))
  }

  def setDefaultParams(): Unit = {
    val maybeSerializer = conf.getOption("spark.serializer")
    val maybeKryoReferenceTracking = conf.getOption("spark.kryo.referenceTracking")
    conf.set("spark.serializer",
      maybeSerializer.getOrElse("org.apache.spark.serializer.KryoSerializer"))
      .set("spark.kryo.referenceTracking",
        maybeKryoReferenceTracking.getOrElse("false"))
    ()
  }

}
