package wookie.sqlserver

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.hadoop.fs.PathFilter

/**
 * @author lukaszjastrzebski
 */
object ListingFilesTest extends App {
  
  val directory = "/Users/lukaszjastrzebski/Projects/tables/ppp3__parquet"
  
  val files = DirectoryObserver.listFiles(new Configuration(), directory)
  
  println(files.mkString("\n"))
}