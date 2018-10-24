package be.cetic.backend.datastream

import be.cetic.backend.datastream.TimedCounter.CounterConfig
import be.cetic.backend.datastream.TimedIterator.Config

import scala.concurrent.duration.FiniteDuration

object TimedCounter {

  case class CounterConfig(upTo: Int, delay: FiniteDuration) extends Config{
    override val description = s"Counter up to $upTo"
  }
}

class TimedCounter(val config: CounterConfig) extends TimedIterator[String] {

  val iterator: Iterator[String] = Iterator.range(0, config.upTo).map(_.toString)

   def computeNextDelay(next: String): FiniteDuration = config.delay
}
