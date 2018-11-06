package be.cetic.tsaas

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import be.cetic.tsaas.websocket.WebSocketService

class TSaaService(implicit val system: ActorSystem, val materializer: ActorMaterializer) extends WebSocketService {

  val routes = Route.seal(
    webSocketRoute
  )
}
