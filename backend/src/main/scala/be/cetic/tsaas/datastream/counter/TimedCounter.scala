package be.cetic.tsaas.datastream.counter

import be.cetic.tsaas.datastream.TimedIterator
import be.cetic.tsaas.datastream.TimedIterator.StreamConfig
import be.cetic.tsaas.datastream.counter.TimedCounter.CounterStreamConfig
import io.swagger.v3.oas.annotations.media.Schema

import scala.concurrent.duration.FiniteDuration

object TimedCounter {

  case class FiniteDurationSchema(@Schema(required=true) length: Int,
                                  @Schema(required=true, allowableValues = Array("nano","micro","milli", "seconds", "minute", "hour", "day")) units: String)

  case class CounterStreamConfig(@Schema(required = true, implementation = classOf[Int]) upTo: Int,
                                 @Schema(required = true,implementation = classOf[FiniteDurationSchema]) delay: FiniteDuration) extends StreamConfig{
    @Schema(hidden=true)
    override val description = s"Counter up to $upTo, with delay $delay"
  }
}

class TimedCounter(val config: CounterStreamConfig) extends TimedIterator[String] {

  val iterator: Iterator[(Long, String)] = Iterator.range(0, config.upTo).map(i=> (0,i.toString))

   def computeNextDelay(current: Option[(Long, String)], next: Option[(Long, String)]): FiniteDuration = config.delay
}