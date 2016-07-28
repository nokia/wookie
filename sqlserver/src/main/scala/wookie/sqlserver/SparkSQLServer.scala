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
import org.apache.spark.sql.SparkSession.Builder
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.sql.hive.thriftserver.HiveThriftServer2
import org.rogach.scallop.ScallopConf
import wookie.spark.cli.{Input, Name, SparkApp}
import org.log4s._

trait SparkSQLServerConf extends Input with Name {
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
      tuples.filter(_.isDefined).map(_.get).toMap
  }
}

object SparkSQLServer extends SparkApp[SparkSQLServerConf](new ScallopConf(_) with SparkSQLServerConf) {

  private[this] val log = getLogger

  override def configure(conf: SparkConf, sessionBuilder: Builder): Builder = super.configure(conf, sessionBuilder).enableHiveSupport()

  def run(opt: SparkSQLServerConf): Unit = {
    processCliParams(opt.hiveconfs.get.getOrElse(Map()))
    setDefaultParams()

    sc.addSparkListener(new StatsReportListener())

    TableRegister(session).setupTableRegistration(opt.inputURL())

    log.info("Tables registered proceeding with launch...")

    HiveThriftServer2.startWithContext(session.sqlContext)
  }

  def processCliParams(arg: Map[String, String]): Unit = {
    for ((k, v) <- arg) System.setProperty(k, v)
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
