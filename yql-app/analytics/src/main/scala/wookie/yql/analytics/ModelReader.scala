package wookie.yql.analytics

import wookie.spark.SparkApp
import wookie.spark.cli.Name
import org.rogach.scallop.ScallopConf
import org.apache.spark.mllib.classification.LogisticRegressionModel
import org.apache.spark.mllib.linalg.Vectors

/**
 * @author lukaszjastrzebski
 */
object ModelReader extends SparkApp[Name](new ScallopConf(_) with Name) { 
  def run(opt: Name): Unit = {
     val model  = LogisticRegressionModel.load(sc, "/Users/lukaszjastrzebski/Projects/LGModel")
      val result = model.predict(Vectors.dense(0.0, 1.1, 0.1))
      println(result)
  }
}