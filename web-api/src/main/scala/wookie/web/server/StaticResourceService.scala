package wookie.web.server

import java.io.File

import org.http4s.server.HttpService
import wookie.app.file.FileService

object StaticResourceService {

  val service: (FileService, File) => HttpService = (filer, rootDirectory) => HttpService {
    case req if filer.ifContains(rootDirectory, req.pathInfo) => 
      filer.contents(rootDirectory, req.pathInfo, req)
  }
  
}