package be.cetic.tsaas.websocket

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import be.cetic.tsaas.datastream.TimedIteratorConfigJsonProtocol
import be.cetic.tsaas.utils.json.UUIDJsonProtocol
import be.cetic.tsaas.websocket.WebsocketActor.{Start, StatusRequest, Stop, Validate}
import be.cetic.tsaas.websocket.WebsocketFactory.WebsocketConfig
import spray.json.{DefaultJsonProtocol, DeserializationException, JsObject, JsString, JsValue, RootJsonFormat}

trait WebsocketActorJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport with UUIDJsonProtocol with TimedIteratorConfigJsonProtocol {
  implicit val statusJsonFormat: RootJsonFormat[WebsocketActor.Status] = jsonFormat4(WebsocketActor.Status)

  implicit val websocketConfigJsonFormat: RootJsonFormat[WebsocketConfig] = jsonFormat2(WebsocketConfig)

  implicit object OperationJsonFormat extends RootJsonFormat[WebsocketActor.Operation] {
    def write(obj: WebsocketActor.Operation): JsValue = {
      val value = obj match {
        case Stop => "stop"
        case Start => "start"
        case StatusRequest => "status"
        case Validate => "validate"
        case m => throw DeserializationException(s"${m} deserialization not implemented")
      }
      JsObject(Map("action" -> JsString(value)))
    }

    override def read(json: JsValue): WebsocketActor.Operation = {
      json.asJsObject.fields("action").asInstanceOf[JsString].value match {
        case "stop" => Stop
        case "start" => Start
        case "status" => StatusRequest
        case "validate" => Validate
      }
    }
  }

}
