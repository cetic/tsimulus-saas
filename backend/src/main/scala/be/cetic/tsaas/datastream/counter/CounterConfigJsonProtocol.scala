package be.cetic.tsaas.datastream.counter

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, JsNumber, JsObject, JsString, JsValue, RootJsonFormat}

import scala.concurrent.duration.FiniteDuration

trait CounterConfigJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {

  implicit object CounterConfigJsonFormat extends RootJsonFormat[TimedCounter.CounterConfig] {
    def write(config: TimedCounter.CounterConfig): JsValue = {
      val durationUnit = config.delay.unit match {
        case java.util.concurrent.TimeUnit.DAYS => "day"
        case java.util.concurrent.TimeUnit.HOURS => "hour"
        case java.util.concurrent.TimeUnit.MICROSECONDS => "microsecond"
        case java.util.concurrent.TimeUnit.MILLISECONDS => "millisecond"
        case java.util.concurrent.TimeUnit.MINUTES => "minute"
        case java.util.concurrent.TimeUnit.NANOSECONDS => "nanosecond"
        case java.util.concurrent.TimeUnit.SECONDS => "second"
      }
      val description = config.description
      val jsDelay = JsObject(
        Map(
          "length" -> JsNumber(config.delay.length),
          "unit" -> JsString(durationUnit)
        )
      )
      JsObject(Map("up_to" -> JsNumber(config.upTo), "delay" -> jsDelay, "type" -> JsString("counter"), "description" -> JsString(description)))
    }

    def read(json: JsValue): TimedCounter.CounterConfig = {
      val jsonMap = json.asJsObject().fields
      val upTo = jsonMap("up_to").asInstanceOf[JsNumber].value.toInt
      val jsDelay = jsonMap("delay").asJsObject().fields
      val length = jsDelay("length").asInstanceOf[JsNumber].value.toInt
      val unit = jsDelay("unit").asInstanceOf[JsString].value
      TimedCounter.CounterConfig(upTo, FiniteDuration(length, unit))
    }
  }
}
