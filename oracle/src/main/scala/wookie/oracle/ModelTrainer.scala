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