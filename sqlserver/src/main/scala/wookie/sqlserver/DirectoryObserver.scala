/*
 * Copyright (C) 2014-2015 by Nokia.
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
 *
 */
package wookie.sqlserver

import java.util.concurrent.ScheduledExecutorService

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{Path, PathFilter}

import scala.concurrent.duration.Duration
import scalaz.concurrent.Task
import scalaz.stream.time

case class Difference(added: Set[String], removed: Set[String])

object DirectoryObserver {

  private var recentlySelectedFiles = new scala.collection.mutable.HashSet[String]()

  def listDirectory(conf: Configuration, directory: String): Set[String] = {
    val path = new Path(directory)
    val fs = path.getFileSystem(conf)

    val filter = new PathFilter {
      def accept(path: Path): Boolean = fs.isDirectory(path)
    }
    fs.listStatus(path, filter).map(_.getPath.toString).toSet
  }

  def calculateDifference(state: Vector[Set[String]]): Option[Difference] = {
    if (state.size == 2) {
      val previous = state(0)
      val current = state(1)
      Some(Difference(current.diff(previous), previous.diff(current)))
    } else {
      None
    }
  }

  def listFiles(conf: Configuration, directory: String): Set[String] = {
    val path = new Path(directory)
    val fs = path.getFileSystem(conf)

    val result = collection.mutable.Set[String]()
    val queue = new collection.mutable.Queue[Path]()

    queue.enqueue(path)
    while (queue.nonEmpty) {
      val current = queue.dequeue()
      val subFiles = fs.listStatus(current)
      val (dirs, files) = subFiles.partition(f => fs.isDirectory(f.getPath))
      dirs.foreach(x => queue.enqueue(x.getPath))
      files.map(a => s"${a.getPath.toString}|${a.getModificationTime}").foreach(result.add)
    }
    result.toSet
  }

  def observeDirectories(conf: Configuration, directory: String, duration: Duration)(
    f: Option[Difference] => Unit)(implicit scheduler: ScheduledExecutorService): scalaz.stream.Process[Task, Unit] = {
    observe(listDirectory)(conf, directory, duration)(f)
  }

  def observeFilesRecursively(conf: Configuration, directory: String, duration: Duration)
    (f: Option[Difference] => Unit)(implicit scheduler: ScheduledExecutorService): scalaz.stream.Process[Task, Unit] = {
    observe(listFiles)(conf, directory, duration)(f)
  }

  private def observe(filter: (Configuration, String) => Set[String])(conf: Configuration, directory: String, duration: Duration)
    (f: Option[Difference] => Unit)(implicit scheduler: ScheduledExecutorService) = {
    time.awakeEvery(duration)
    .map(src => filter(conf, directory))
    .sliding(2)
    .map(calculateDifference)
    .map(f)
  }

}
