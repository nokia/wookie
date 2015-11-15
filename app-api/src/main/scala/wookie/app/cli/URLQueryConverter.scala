package wookie.app.cli

import org.rogach.scallop.ValueConverter
import org.rogach.scallop.ArgType

object URLQueryConverter extends ValueConverter[Map[String, String]] {

  def parseParam(queryParam: String): Option[(String, String)] = {
    val queryEntries = queryParam.split("=", -1)
    val key = queryEntries(0)
    val value = queryEntries.drop(1).mkString("=")
    Some((key -> value))
  }

  def parse(s: List[(String, List[String])]): Either[String, Option[Map[String, String]]] = {
    val queryMap = for {
      elem <- s
      singleElem <- elem._2
      queryProp <- singleElem.split("&", -1)
      p <- parseParam(queryProp) if !queryProp.trim.isEmpty()
    } yield {
      p
    }
    Right(Some(queryMap.toMap))
  }

  val tag = scala.reflect.runtime.universe.typeTag[Map[String, String]]
  val argType: ArgType.V = org.rogach.scallop.ArgType.SINGLE
}
