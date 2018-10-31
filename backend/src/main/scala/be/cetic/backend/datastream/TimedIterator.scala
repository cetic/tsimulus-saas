package be.cetic.backend.datastream

import be.cetic.backend.datastream.TimedIterator.Config
import be.cetic.backend.datastream.counter.TimedCounter
import be.cetic.backend.datastream.tsimulus.TsimulusIterator

import scala.concurrent.duration.FiniteDuration

object TimedIterator {

  trait Config {
    val description: String = ""
  }

  def factory[T](config: Config): TimedIterator[T] = {

    config match {
      case c: TimedCounter.CounterConfig => new TimedCounter(c)
      case c: TsimulusIterator.TsimulusConfig => new TsimulusIterator(c)
      case _ => throw new Exception("Undefined timed iterator type")
    }
  }.asInstanceOf[TimedIterator[T]]
}


trait TimedIterator[T] {
  val config: Config
  val iterator: Iterator[(Long, T)]
  val dropBeforeNow = false

  private var initialized = false
  private var nextElement: Option[(Long, T)] = None

  def computeNextDelay(current: Option[(Long, T)], next: Option[(Long, T)]): FiniteDuration


  def duplicate: (TimedIterator[T], TimedIterator[T]) = (this, TimedIterator.factory(config))

  def next(): Option[(FiniteDuration, T)] = {
    if (!initialized) {
      nextElement = prepareNextElement()
      if (dropBeforeNow) filterBeforeNow()
      initialized = true
    }
    val thisElement = nextElement
    nextElement = prepareNextElement()
    thisElement.map(next => (computeNextDelay(thisElement, nextElement), next._2))
  }

  def filterBefore(before: Long): Unit = {
    var delay : Long = nextElement.map(_._1 - before).getOrElse(0)

    while (delay < 0) {
      nextElement = prepareNextElement()
      delay = nextElement.map(_._1 - before).getOrElse(0)
    }
  }

  private def filterBeforeNow() : Unit= filterBefore(System.currentTimeMillis())

  private def prepareNextElement(): Option[(Long, T)] = {
    if (iterator.hasNext) Some(iterator.next()) else None
  }
}
