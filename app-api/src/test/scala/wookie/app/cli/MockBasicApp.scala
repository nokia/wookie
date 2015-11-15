package wookie.app.cli

import org.rogach.scallop.ScallopConf

trait AllConf extends Input with Topics with URLQuery

object MockBasicApp extends BasicApp[AllConf](a => new ScallopConf(a) with AllConf) {
  var inputURL: String = null
  var query: Map[String, String] = null
  var topics: List[String] = null

  override def run(opt: AllConf): Unit = {
    inputURL = opt.inputURL()
    query = opt.query()
    topics = opt.topics()
  }
}