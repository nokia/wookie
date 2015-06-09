package wookie.spark.sparkle

import scala.reflect.ClassTag
import scalaz._

import org.apache.spark.streaming.dstream.DStream

import wookie.spark.SparkApp
import wookie.spark.SparkStreamingApp

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

object Sparkles {
  def map[A, B](stream: DStream[A])(f: A => B)(implicit a: ClassTag[A], b: ClassTag[B]): Sparkle[DStream[B], SparkStreamingApp[_]] = new Sparkle[DStream[B], SparkStreamingApp[_]] {

    def run(app: SparkStreamingApp[_]): DStream[B] = {
      stream.map(f)
    }
  }

  def filter[A](stream: DStream[A])(f: A => Boolean)(implicit a: ClassTag[A]): Sparkle[DStream[A], SparkStreamingApp[_]] = new Sparkle[DStream[A], SparkStreamingApp[_]] {

    def run(app: SparkStreamingApp[_]): DStream[A] = {
      stream.filter(f)
    }
  }
  
  def flatMap[A, U](stream: DStream[A])(f: A => Traversable[U])(implicit a: ClassTag[A], u: ClassTag[U]): Sparkle[DStream[U], SparkStreamingApp[_]] = new Sparkle[DStream[U], SparkStreamingApp[_]] {

    def run(app: SparkStreamingApp[_]): DStream[U] = {
      stream.flatMap(f)
    }
  }  
  
  def foldFilters[A](f1: A => Boolean, fs: A => Boolean*)(implicit boolM: Monoid[Boolean]): A => Boolean = s => {
    fs.foldLeft(f1(s))((t1, t2) => boolM.append(t2(s), t1))
  }  
}