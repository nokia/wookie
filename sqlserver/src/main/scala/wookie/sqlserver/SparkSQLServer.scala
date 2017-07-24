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

import org.apache.spark.SparkConf
import org.apache.spark.scheduler.StatsReportListener
import org.apache.spark.sql.{SQLImplicits, SparkSession}
import org.apache.spark.sql.SparkSession.Builder
import org.apache.spark.sql.hive.thriftserver.HiveThriftServer2
import org.rogach.scallop.ScallopConf
import wookie.spark.cli.{Input, Name, SparkApp}
import org.log4s._

trait KVParser {
  def asMap(lst: List[String]): Map[String, String] = {
    val tuples = lst.map {
      x =>
        val splitted = x.split("=", -1)
        if (splitted.size == 2) {
          Some((splitted(0), splitted(1)))
        } else {
          None
        }
    }
    tuples.filter(_.isDefined).map(_.get).toMap
  }
}

trait HiveConf extends ScallopConf with KVParser {
  lazy val hiveconfs = opt[List[String]]("hiveconf", descr = "hive configs").map(asMap)
}

trait MultipleInputConf extends ScallopConf with KVParser {
  lazy val inputs = opt[List[String]]("input", descr = "input").map(asMap)
}

abstract class CommonSQLServer[A](options: Array[String] => HiveConf with Name with A) extends SparkApp[HiveConf with Name with A](options) {

  private[this] val log = getLogger

  override def configure(conf: SparkConf, sessionBuilder: Builder): Builder = super.configure(conf, sessionBuilder).enableHiveSupport()

  def processCliParams(arg: Map[String, String]): Unit = {
    for ((k, v) <- arg) System.setProperty(k, v)
  }

  override def run(opt: HiveConf with Name with A, spark: SparkSession): Unit = {
    processCliParams(opt.hiveconfs.get.getOrElse(Map()))
    setDefaultParams(spark.sparkContext.getConf)

    spark.sparkContext.addSparkListener(new StatsReportListener())

    registerTables(opt, spark)

    log.info("Tables registered proceeding with launch...")

    HiveThriftServer2.startWithContext(spark.sqlContext)
  }

  def setDefaultParams(conf: SparkConf): Unit = {
    val maybeSerializer = conf.getOption("spark.serializer")
    val maybeKryoReferenceTracking = conf.getOption("spark.kryo.referenceTracking")
    conf.set("spark.serializer",
      maybeSerializer.getOrElse("org.apache.spark.serializer.KryoSerializer"))
      .set("spark.kryo.referenceTracking",
        maybeKryoReferenceTracking.getOrElse("false"))
    ()
  }

  def registerTables(opt: HiveConf with Name with A, spark: SparkSession): Unit
}

object SparkSQLServer extends CommonSQLServer[Name with Input](new ScallopConf(_) with HiveConf with Name with Input) {

  override def registerTables(opt: HiveConf with Name with Input, spark: SparkSession): Unit = {
    TableRegister(spark).setupTableRegistration(opt.inputURL())
  }

}

object ParquetSQLServer extends CommonSQLServer[Name with MultipleInputConf](new ScallopConf(_) with HiveConf with Name with MultipleInputConf) {

  override def registerTables(opt: HiveConf with Name with MultipleInputConf, spark: SparkSession): Unit = {
    val inputMap = opt.inputs.get.getOrElse(Map())
    TableRegister(spark).setupTableRegistration(inputMap)
  }

}
