package wookie.web.cli

import org.rogach.scallop.ScallopConf

trait Port extends ScallopConf {
  lazy val port = opt[Int]("port", descr = "Port to listent to", required = true)
}

