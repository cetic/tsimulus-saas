package be.cetic.backend.websocket

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives.{handleWebSocketMessages, path, _}
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout
import be.cetic.backend.datastream.TimedCounter

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
            wsFactory.createOrUpdateConfig(TimedCounter.CounterConfig(10, 1 second))
              .toString
          }
        } ~
          get {
            complete {
              wsFactory.readConfigs().toString()
            }
          }
      } ~
        pathPrefix(JavaUUID) { wsId =>
          pathEnd {
            post {
              complete {
                wsFactory.createOrUpdateConfig(TimedCounter.CounterConfig(10, 1 second), wsId)
                  .toString
              }
            } ~
              get {
                complete {
                  wsFactory.readConfig(wsId).toString()
                }
              }
          } ~
            path("start") {
              post {
                complete {
                  wsFactory.startStream(wsId)
                }
              }
            }~
          path("ws"){
            handleWebSocketMessages {
              wsFactory.getOrCreateWebsocketHandler(wsId).flow
            }
          }
        }
    }
}


