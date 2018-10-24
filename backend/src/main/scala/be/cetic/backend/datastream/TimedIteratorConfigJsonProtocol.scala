package be.cetic.backend.datastream

import be.cetic.backend.datastream.counter.{CounterConfigJsonProtocol, TimedCounter}
import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, RootJsonFormat}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport


trait TimedIteratorConfigJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport with CounterConfigJsonProtocol{

  implicit object ConfigJsonFormat extends RootJsonFormat[TimedIterator.Config] {
    override def read(json: JsValue): TimedIterator.Config = {
      val jsMap = json.asJsObject().fields
      jsMap("type").asInstanceOf[JsString].value match {
        case "counter" => CounterConfigJsonFormat.read(json)
        case otherType => throw DeserializationException(s"$otherType json format not implemented.")
      }
    }

    override def write(obj: TimedIterator.Config): JsValue = {
      obj match {
        case c: TimedCounter.CounterConfig => CounterConfigJsonFormat.write(c)
        case otherType => throw DeserializationException(s"$otherType json format not implemented.")
      }
    }
  }

}
