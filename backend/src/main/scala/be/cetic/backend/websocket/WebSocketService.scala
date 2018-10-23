package be.cetic.backend.websocket

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives.{handleWebSocketMessages, path, _}
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout
import be.cetic.backend.websocket.WebsocketFactory.WsUnavailable

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

trait WebSocketService {
  implicit val materializer: ActorMaterializer
  implicit val system: ActorSystem
  implicit val dispatcher: ExecutionContextExecutor = system.dispatcher
  implicit val timeout: Timeout = Timeout(20.seconds)
  private val wsFactory = new WebsocketFactory

  lazy val webSocketRoute: Route =
    pathPrefix("ws") {
      pathEnd {
        post {
          complete {
            wsFactory
              .createWsEntry(60.seconds)
              .toString
          }
        }
      } ~
        path(JavaUUID) { wsId =>
          val flow = wsFactory.makeWsFlow(wsId)
          println("connection")
          flow.map(handleWebSocketMessages)
            .getOrElse {
              get(complete(WsUnavailable.toString))
            }
        }
    }
}

