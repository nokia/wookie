/* Copyright (C) 2014-2015 by Nokia.
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
*/
package wookie.oracle

//import wookie.spark.SparkApp
//import org.rogach.scallop.ScallopConf
//import wookie.spark.cli.Name
//import org.apache.spark.mllib.regression.LabeledPoint
//import org.apache.spark.mllib.linalg.Vectors
//import org.apache.spark.mllib.classification.LogisticRegressionWithLBFGS
//import wookie.spark.cli.Output
//
//
//trait ModelTrainConf extends Name with Output
//
//object ModelTrainer extends SparkApp[ModelTrainConf](new ScallopConf(_) with ModelTrainConf) {
//
//
//
//  def run(opt: ModelTrainConf): Unit = {
//
//  val training = sc.parallelize(Seq(
//    LabeledPoint(1.0, Vectors.dense(0.0, 1.1, 0.1)),
//    LabeledPoint(0.0, Vectors.dense(2.0, 1.0, -1.0)),
//    LabeledPoint(0.0, Vectors.dense(2.0, 1.3, 1.0)),
//    LabeledPoint(1.0, Vectors.dense(0.0, 1.2, -0.5))))
//
//    val lr = new LogisticRegressionWithLBFGS()
//    lr.optimizer.setNumIterations(10).setRegParam(0.01)
//
//    val model1 = lr.run(training)
//    model1.save(sc, opt.outputURL())
//  }
//}
