package be.cetic.tsaas.websocket

import java.util.UUID

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout
import be.cetic.tsaas.datastream.TimedIteratorConfigJsonProtocol
import be.cetic.tsaas.utils.json.UUIDJsonProtocol
import spray.json._

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

trait WebSocketService extends TimedIteratorConfigJsonProtocol with WebsocketActorJsonProtocol with UUIDJsonProtocol with DefaultJsonProtocol with SprayJsonSupport with StreamConfigurationService {
  implicit val materializer: ActorMaterializer
  implicit val system: ActorSystem
  implicit val dispatcher: ExecutionContextExecutor = system.dispatcher
  implicit val timeout: Timeout = Timeout(20.seconds)
  val wsFactory = new WebsocketFactory

  val webSocketRoute: Route =
    pathPrefix("stream") {
      pathEnd {
        readConfigs() ~ postConfigs()
      } ~
        pathPrefix(JavaUUID) { wsId =>
          pathEnd {
            createOrUpdateConfig(wsId) ~ readConfig(wsId) ~ deleteConfig(wsId)
          } ~
            path("act") {
              act(wsId)
            }
        }
    } ~
      path("socket" / JavaUUID) { wsId => websocket(wsId) }

  def websocket(wsId: UUID): Route = {
    handleWebSocketMessages {
      wsFactory.getOrCreateWebsocketHandler(wsId).flow
    }
  }

  def act(wsId: UUID): Route = {
    post {
      entity(as[WebsocketActor.Operation]) { operation =>
        complete {
          operation match {
            case WebsocketActor.Validate => wsFactory.startStream(wsId, once = true)
            case WebsocketActor.Start => wsFactory.startStream(wsId)
            case WebsocketActor.Stop => wsFactory.stopStream(wsId)
            case WebsocketActor.StatusRequest => wsFactory.streamStatus(wsId)
            case _ => HttpResponse(StatusCodes.NotImplemented)
          }

        }
      }
    }
  }

}


