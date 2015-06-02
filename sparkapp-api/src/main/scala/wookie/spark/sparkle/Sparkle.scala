package wookie.spark.sparkle

import wookie.spark.SparkApp

trait Sparkle[A, APP <: SparkApp[_]] { parent =>
  def run(app: APP): A

  def flatMap[B](f: A => Sparkle[B, APP]): Sparkle[B, APP] = new Sparkle[B, APP] {
    def run(app: APP): B = {
      val parentResult: A = parent.run(app)
      val nextSparkle: Sparkle[B, APP] = f(parentResult)
      nextSparkle.run(app)
    }
  }

  def map[B](f: A => B): Sparkle[B, APP] = new Sparkle[B, APP] {
    def run(app: APP): B = {
      f(parent.run(app))
    }
  }
}