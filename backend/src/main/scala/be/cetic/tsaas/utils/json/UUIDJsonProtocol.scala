package be.cetic.tsaas.utils.json

import java.util.UUID

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, RootJsonFormat}

trait UUIDJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport{

  implicit object UuidJsonFormat extends RootJsonFormat[UUID] {
    def write(x: UUID) = JsString(x.toString)

    def read(value: JsValue): UUID = value match {
      case JsString(x) => UUID.fromString(x)
      case _ => throw DeserializationException("String expected")
    }
  }

}
