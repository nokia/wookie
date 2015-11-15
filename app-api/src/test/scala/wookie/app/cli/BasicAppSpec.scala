package wookie.app.cli

import org.junit.runner.RunWith
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

/**
  * Created by ljastrze on 11/14/15.
  */
@RunWith(classOf[JUnitRunner])
class BasicAppSpec extends Specification with ScalaCheck {

  "Basic App needs to make properties accessible in run method" >> prop { (inputURL: String, query: Map[String, String],
                                                                           topics: List[String]) =>
    val cleanedQuery = query.map { case (a, b) => (a.replaceAll("=|&", ""), b.replaceAll("=|&", "")) }
    val queryStr = cleanedQuery.map { case (a, b) => s"${a}=${b}" }.mkString("&")
    val topicList = topics.map(a => List("--topics", a)).foldLeft(List[String]())((a, b) => a ++ b)
    val cli = List("--input", inputURL, "--query", queryStr) ++ topicList

    MockBasicApp.main(cli.toArray)
    MockBasicApp.inputURL must_== inputURL
    MockBasicApp.query must_== cleanedQuery
    MockBasicApp.topics must_== topics
  }
}


