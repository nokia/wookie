package wookie.app.cli

import org.rogach.scallop.ScallopConf

abstract class BasicApp[A <: ScallopConf](options: Array[String] => A) {

  def run(opt: A): Unit
  
  final def main(args: Array[String]): Unit = {
    val opt = options(args)
    opt.afterInit()
    opt.assertVerified()
    run(opt)
  }
}