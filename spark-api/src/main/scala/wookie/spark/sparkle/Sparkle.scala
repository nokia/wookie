package wookie.spark.sparkle

import wookie.spark.cli.{SparkApp, SparkStreamingApp}

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
  def apply[A](f: => A) = new Sparkle[A, SparkApp[_]] {
    def apply(app: SparkApp[_]): A = {
      f
    }
  }
}

object StreamingSparkle {
  def apply[A](f: => A) = new Sparkle[A, SparkStreamingApp[_]] {
    def apply(app: SparkStreamingApp[_]): A = {
      f
    }
  }
}