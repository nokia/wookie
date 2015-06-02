package wookie.sqlserver

import java.util.concurrent.ScheduledExecutorService
import scala.concurrent.duration.Duration
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.hadoop.fs.PathFilter
import scalaz.stream.time
import scala.annotation.tailrec
import org.apache.hadoop.fs.FileStatus

case class Difference(added: Set[String], removed: Set[String])

object DirectoryObserver {

  private var recentlySelectedFiles = new scala.collection.mutable.HashSet[String]()

  def listDirectory(conf: Configuration, directory: String) = {
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
  
  def listFiles(conf: Configuration, directory: String) = {
    val path = new Path(directory)
    val fs = path.getFileSystem(conf)

    val result = collection.mutable.Set[String]()
    val queue = new collection.mutable.Queue[Path]()
      
    queue.enqueue(path)
    while (!queue.isEmpty) {
      val current = queue.dequeue()
      val subFiles = fs.listStatus(current)
      val (dirs, files) = subFiles.partition(f => fs.isDirectory(f.getPath))    
      dirs.foreach(x => queue.enqueue(x.getPath))
      files.map(a => s"${a.getPath.toString}|${a.getModificationTime}").foreach(result.add(_))
    }
    result.toSet
  }  
  
  def observeDirectories(conf: Configuration, directory: String, duration: Duration)(
    f: Option[Difference] => Unit)(implicit scheduler: ScheduledExecutorService) = {
    observe(listDirectory)(conf, directory, duration)(f)
  }
  
  def observeFilesRecursively(conf: Configuration, directory: String, duration: Duration)
    (f: Option[Difference] => Unit)(implicit scheduler: ScheduledExecutorService) = {
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