package be.cetic.backend.datastream.counter

import be.cetic.backend.datastream.TimedIterator
import be.cetic.backend.datastream.TimedIterator.Config
import be.cetic.backend.datastream.counter.TimedCounter.CounterConfig

import scala.concurrent.duration.FiniteDuration

object TimedCounter {

  case class CounterConfig(upTo: Int, delay: FiniteDuration) extends Config{
    override val description = s"Counter up to $upTo, with delay $delay"
  }
}

class TimedCounter(val config: CounterConfig) extends TimedIterator[String] {

  val iterator: Iterator[String] = Iterator.range(0, config.upTo).map(_.toString)

   def computeNextDelay(current: Option[String], next: Option[String]): FiniteDuration = config.delay
}
