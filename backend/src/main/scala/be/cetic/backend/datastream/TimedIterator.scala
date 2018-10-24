package be.cetic.backend.datastream

import be.cetic.backend.datastream.TimedIterator.Config

import scala.concurrent.duration.FiniteDuration

object TimedIterator {

  trait Config {
    val description: String = ""
  }

  def factory[T](config: Config): TimedIterator[T] = {

    config match {
      case c: TimedCounter.CounterConfig => new TimedCounter(c)
      case _ => throw new Exception("Undefined timed iterator type")
    }
  }.asInstanceOf[TimedIterator[T]]
}


trait TimedIterator[T] {
  val config: Config
  val iterator: Iterator[T]

  private var initialized= false
  private var nextElement: Option[T] = None

  def computeNextDelay(next: T): FiniteDuration

  def next(): Option[(FiniteDuration, T)] = {
    if (!initialized) {
      nextElement = prepareNextElement()
      initialized = true
    }
    val thisElement = nextElement
    nextElement = prepareNextElement()
    thisElement.map(next => (computeNextDelay(next), next))
  }

  private def prepareNextElement(): Option[T] = {
    if (iterator.hasNext) Some(iterator.next()) else None
  }
}
