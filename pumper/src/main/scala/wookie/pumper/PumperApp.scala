package wookie.pumper

import org.http4s.server._
import wookie.app.cli._
import org.rogach.scallop.ScallopConf
import wookie.web.cli.{Port, HttpProducer}

trait PumperAppConf extends Port 

object PumperApp extends HttpProducer[PumperAppConf](new ScallopConf(_) with PumperAppConf) {

  def createServices(a: PumperAppConf): Map[String, HttpService] = {
    Map("/ingest" -> Ingestion.service)
  }
  
}