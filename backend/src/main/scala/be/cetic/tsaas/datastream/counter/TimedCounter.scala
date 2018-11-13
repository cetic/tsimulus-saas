package be.cetic.tsaas.datastream.counter

import be.cetic.tsaas.datastream.TimedIterator
import be.cetic.tsaas.datastream.TimedIterator.Config
import be.cetic.tsaas.datastream.counter.TimedCounter.CounterConfig
import io.swagger.v3.oas.annotations.media.Schema

import scala.concurrent.duration.FiniteDuration

object TimedCounter {

  case class FiniteDurationSchema(@Schema(required=true) length: Int,
                                  @Schema(required=true, allowableValues = Array("nano","micro","milli", "seconds", "minute", "hour", "day")) units: String)

  case class CounterConfig(@Schema(required = true, implementation = classOf[Int]) upTo: Int,
                           @Schema(required = true,implementation = classOf[FiniteDurationSchema]) delay: FiniteDuration) extends Config{
    @Schema(hidden=true)
    override val description = s"Counter up to $upTo, with delay $delay"
  }
}

class TimedCounter(val config: CounterConfig) extends TimedIterator[String] {

  val iterator: Iterator[(Long, String)] = Iterator.range(0, config.upTo).map(i=> (0,i.toString))

   def computeNextDelay(current: Option[(Long, String)], next: Option[(Long, String)]): FiniteDuration = config.delay
}