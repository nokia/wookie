package wookie.oracle

import org.http4s.server._
import wookie.app.cli._
import org.rogach.scallop.ScallopConf
import wookie.web.cli.{Port, HttpProducer}

trait OracleAppConf extends Port

object OracleApp extends HttpProducer[OracleAppConf](new ScallopConf(_) with OracleAppConf) {

  def createServices(a: OracleAppConf): Map[String, HttpService] = {
    Map("/predict" -> Prediction.service)
  }

}
