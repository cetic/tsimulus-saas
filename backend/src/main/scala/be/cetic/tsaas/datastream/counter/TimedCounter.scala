package be.cetic.tsaas.datastream.counter

import be.cetic.tsaas.datastream.TimedIterator
import be.cetic.tsaas.datastream.TimedIterator.StreamConfig
import be.cetic.tsaas.datastream.counter.TimedCounter.CounterStreamConfig

import scala.concurrent.duration.FiniteDuration

object TimedCounter {


  case class CounterStreamConfig( upTo: Int,
                                 delay: FiniteDuration) extends StreamConfig{

    override val description = s"Counter up to $upTo, with delay $delay"
  }
}

class TimedCounter(val config: CounterStreamConfig) extends TimedIterator[String] {

  val iterator: Iterator[(Long, String)] = Iterator.range(0, config.upTo).map(i=> (0,i.toString))

   def computeNextDelay(current: Option[(Long, String)], next: Option[(Long, String)]): FiniteDuration = config.delay
}