package be.cetic.tsaas.websocket

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait WebsocketActorJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport{
  implicit val statusJsonFormat : RootJsonFormat[WebsocketActor.Status] = jsonFormat3(WebsocketActor.Status)
}
