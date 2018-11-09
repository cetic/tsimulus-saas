package be.cetic.tsaas

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import be.cetic.tsaas.websocket.{SwaggerDocService, WebSocketService}
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors

class TSaaService(implicit val system: ActorSystem, val materializer: ActorMaterializer) extends WebSocketService {

  val routes = Route.seal {
    cors() {
      webSocketRoute ~
        SwaggerDocService.routes
    }
  }
}
