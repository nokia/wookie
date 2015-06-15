package wookie.spark.sparkle

import wookie.spark.SparkApp

trait Sparkle[A, APP <: SparkApp[_]] { parent =>
  def apply(app: APP): A

  def flatMap[B](f: A => Sparkle[B, APP]): Sparkle[B, APP] = new Sparkle[B, APP] {
    def apply(app: APP): B = {
      val parentResult: A = parent(app)
      val nextSparkle: Sparkle[B, APP] = f(parentResult)
      nextSparkle(app)
    }
  }

  def map[B](f: A => B): Sparkle[B, APP] = new Sparkle[B, APP] {
    def apply(app: APP): B = {
      f(parent(app))
    }
  }
}

object Sparkle {
  def apply[A, APP <: SparkApp[_]](f: APP => A) = new Sparkle[A, APP] {
    def apply(app: APP): A = {
      f(app)
    }
  }
}
