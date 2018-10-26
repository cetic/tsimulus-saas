package be.cetic.backend.datastream

import be.cetic.backend.datastream.TimedIterator.Config
import be.cetic.backend.datastream.counter.TimedCounter

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

  def computeNextDelay(current: Option[T], next: Option[T]): FiniteDuration

  def duplicate : (TimedIterator[T], TimedIterator[T]) = (this, TimedIterator.factory(config))

  def next(): Option[(FiniteDuration, T)] = {
    if (!initialized) {
      nextElement = prepareNextElement()
      initialized = true
    }
    val thisElement = nextElement
    nextElement = prepareNextElement()
    thisElement.map(next => (computeNextDelay(thisElement, nextElement), next))
  }

  private def prepareNextElement(): Option[T] = {
    if (iterator.hasNext) Some(iterator.next()) else None
  }
}
