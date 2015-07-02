package wookie.web.server.file

import java.io.File
import org.http4s.Request
import org.http4s.Response
import scalaz.concurrent.Task

trait FileService {

  def ifContains(rootDir: File, path: String): Boolean

  def contents(rootDir: File, path: String, req: Request): Task[Response]
}