package wookie.pumper

import org.http4s.server._
import wookie.app.HttpProducer
import wookie.app.cli._
import org.rogach.scallop.ScallopConf

trait PumperAppConf extends Port 

object PumperApp extends HttpProducer[PumperAppConf](new ScallopConf(_) with PumperAppConf) {

  def createServices(a: PumperAppConf): Map[String, HttpService] = {
    Map("/ingest" -> Ingestion.service)
  }
  
}