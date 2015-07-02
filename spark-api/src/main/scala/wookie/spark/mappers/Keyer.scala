package wookie.spark.mappers

object Keyer {

  def withId[A, B](f: A => B)(elem: A): (B, A) = {
    (f(elem), elem)
  }
}
