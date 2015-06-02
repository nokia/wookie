package wookie.http

import org.http4s.server._
import java.io.File
import wookie.file.FileService

object StaticResourceService {

  val service: (FileService, File) => HttpService = (filer, rootDirectory) => HttpService {
    case req if filer.ifContains(rootDirectory, req.pathInfo) => 
      filer.contents(rootDirectory, req.pathInfo, req)
  }
  
}