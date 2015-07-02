package wookie.app.file

import java.io.File
import org.http4s.StaticFile
import org.http4s.Request
import org.http4s.dsl._
import scalaz.concurrent.Task

class LocalFS extends FileService {

  override def ifContains(rootDir: File, path: String) = new File(rootDir, path).isFile()

  override def contents(rootDir: File, path: String, req: Request) = {
    StaticFile.fromFile(new File(rootDir, req.pathInfo), Some(req)).map(Task.now).getOrElse(NotFound())
  }

}