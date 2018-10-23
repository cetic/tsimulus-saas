package be.cetic.backend.websocket

import java.util.UUID

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives.{handleWebSocketMessages, path}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

trait WebSocketService {
  implicit val materializer: ActorMaterializer
  implicit val system: ActorSystem

  val wsMap = Map[UUID, WebsocketFactory]()


  lazy val webSocketRoute: Route =
    pathPrefix("ws") {
      pathEnd {
        post {
          complete {
            val uuid = UUID.randomUUID()
            wsMap.updated(uuid, WebsocketFactory())
            s"ws/$uuid"
          }
        }
      }
    }
  path("ws" / JavaUUID) { uuid =>
    handleWebSocketMessages(wsMap(uuid).webSocketFlow)
  }

}