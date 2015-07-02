package wookie.spark.cli

import org.apache.spark.{Logging, SparkConf, SparkContext}
import org.apache.spark.sql.SQLContext

abstract class SparkApp[A <: Name](options: Array[String] => A) extends Logging {

  protected var _sc: SparkContext = _
  protected var _conf: SparkConf = _
  protected var _sqlContext: SQLContext = _
  protected var _opt: A = _
  
  def sc = _sc
  def conf = _conf
  def sqlContext = _sqlContext
  def opt = _opt

  def run(opt: A): Unit

  final def main(args: Array[String]): Unit = {
    _opt = options(args)
    _opt.afterInit()
    _opt.assertVerified()
    
    _conf = new SparkConf().setAppName(opt.name())
    _sc = new SparkContext(_conf)
    _sqlContext = new SQLContext(_sc)

    Runtime.getRuntime.addShutdownHook(
      new Thread() {
        override def run() = {
          _sc.stop()
        }
      }
    )
    
    run(opt)

    
  }

}
