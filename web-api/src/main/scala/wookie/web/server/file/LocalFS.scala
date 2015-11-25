/* Copyright (C) 2014-2015 by Nokia.
 * See the LICENCE.txt file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package wookie.web.server.file

import java.io.File
import org.http4s.{Response, StaticFile, Request}
import org.http4s.dsl._
import scalaz.concurrent.Task

class LocalFS extends FileService {

  override def ifContains(rootDir: File, path: String): Boolean = new File(rootDir, path).isFile

  override def contents(rootDir: File, path: String, req: Request): Task[Response] = {
    StaticFile.fromFile(new File(rootDir, req.pathInfo), Some(req)).map(Task.now).getOrElse(NotFound())
  }

}