package be.cetic.tsaas.datastream.counter

import be.cetic.tsaas.datastream.TimedIterator
import be.cetic.tsaas.datastream.TimedIterator.Config
import be.cetic.tsaas.datastream.counter.TimedCounter.CounterConfig

import scala.concurrent.duration.FiniteDuration

object TimedCounter {

  case class CounterConfig(upTo: Int, delay: FiniteDuration) extends Config{
    override val description = s"Counter up to $upTo, with delay $delay"
  }
}

class TimedCounter(val config: CounterConfig) extends TimedIterator[String] {

  val iterator: Iterator[(Long, String)] = Iterator.range(0, config.upTo).map(i=> (0,i.toString))

   def computeNextDelay(current: Option[(Long, String)], next: Option[(Long, String)]): FiniteDuration = config.delay
}