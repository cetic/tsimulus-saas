import be.cetic.tsaas.websocket.WebsocketActor
import spray.json.DefaultJsonProtocol._
import spray.json.{RootJsonFormat, _}

implicit val statusJsonFormat: RootJsonFormat[WebsocketActor.Status] = jsonFormat4(WebsocketActor.Status)

WebsocketActor.Status(true, true, true, None).toJson

Stream(1,2,3,4).filter(_ >3)