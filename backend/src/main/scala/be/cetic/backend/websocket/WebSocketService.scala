package be.cetic.backend.websocket

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives.{handleWebSocketMessages, path, _}
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout
import be.cetic.backend.datastream.{TimedIterator, TimedIteratorConfigJsonProtocol}
import be.cetic.backend.datastream.counter.TimedCounter
import be.cetic.backend.utils.json.UUIDJsonProtocol

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import spray.json._

trait WebSocketService extends TimedIteratorConfigJsonProtocol with UUIDJsonProtocol with DefaultJsonProtocol with SprayJsonSupport {
  implicit val materializer: ActorMaterializer
  implicit val system: ActorSystem
  implicit val dispatcher: ExecutionContextExecutor = system.dispatcher
  implicit val timeout: Timeout = Timeout(20.seconds)
  private val wsFactory = new WebsocketFactory

  lazy val webSocketRoute: Route =
    pathPrefix("stream") {
      pathEnd {
        post {
          complete {
            wsFactory.createOrUpdateConfig(TimedCounter.CounterConfig(10, 1 second))
          }
        } ~
          get {
            complete {
              wsFactory.readConfigs()
            }
          }
      } ~
        pathPrefix(JavaUUID) { wsId =>
          pathEnd {
            post {
              entity(as[TimedIterator.Config]) { config =>
                complete {
                  wsFactory.createOrUpdateConfig(config, wsId)
                }
              }
            } ~
              get {
                complete {
                  wsFactory.readConfig(wsId)
                }
              } ~
              delete {
                complete {
                  wsFactory.deleteConfig(wsId)
                  s"Configuration $wsId deleted."
                }
              }
          } ~
            path("start") {
              post {
                complete {
                  wsFactory.startStream(wsId)
                }
              }
            } ~
            path("stop") {
              post {
                complete {
                  wsFactory.stopStream(wsId)
                }
              }
            } ~
            path("ws") {
              handleWebSocketMessages {
                wsFactory.getOrCreateWebsocketHandler(wsId).flow
              }
            }
        }
    }


}


