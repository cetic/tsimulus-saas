package be.cetic.tsaas

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import be.cetic.tsaas.websocket.{SwaggerDocService, SwaggerTest, WebSocketService}
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors

class TSaaService(implicit val system: ActorSystem, val materializer: ActorMaterializer) extends WebSocketService{

  val sw = new SwaggerTest()

  val routes = Route.seal{cors() {
    webSocketRoute ~
      sw.swRoutes ~
      SwaggerDocService.routes
  }}
}
