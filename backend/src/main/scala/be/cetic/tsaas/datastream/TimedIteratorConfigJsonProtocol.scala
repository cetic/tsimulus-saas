package be.cetic.tsaas.datastream

import be.cetic.tsaas.datastream.counter.{CounterConfigJsonProtocol, TimedCounter}
import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, RootJsonFormat}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import be.cetic.tsaas.datastream.tsimulus.{TsimulusStreamConfigJsonProtocol, TsimulusIterator}


trait TimedIteratorConfigJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport with CounterConfigJsonProtocol with TsimulusStreamConfigJsonProtocol {

  implicit object ConfigJsonFormat extends RootJsonFormat[TimedIterator.StreamConfig] {
    override def read(json: JsValue): TimedIterator.StreamConfig = {
      val jsMap = json.asJsObject().fields
      jsMap("type").asInstanceOf[JsString].value match {
        case "counter" => CounterConfigJsonFormat.read(json)
        case "tsimulus" => TsimulusConfigJsonFormat.read(json)
        case otherType => throw DeserializationException(s"$otherType json format not implemented.")
      }
    }

    override def write(obj: TimedIterator.StreamConfig): JsValue = {
      obj match {
        case c: TimedCounter.CounterStreamConfig => CounterConfigJsonFormat.write(c)
        case c: TsimulusIterator.TsimulusStreamConfig => TsimulusConfigJsonFormat.write(c)
        case otherType => throw DeserializationException(s"$otherType json format not implemented.")
      }
    }
  }

}
