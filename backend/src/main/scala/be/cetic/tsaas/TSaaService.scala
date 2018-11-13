package be.cetic.tsaas

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import be.cetic.tsaas.websocket.WebSocketService
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import be.cetic.tsaas.swagger.SwaggerDocService

class TSaaService(implicit val system: ActorSystem, val materializer: ActorMaterializer) extends WebSocketService {

  val routes = Route.seal {
    cors() {
      webSocketRoute ~
        SwaggerDocService.routes
    }
  }
}
