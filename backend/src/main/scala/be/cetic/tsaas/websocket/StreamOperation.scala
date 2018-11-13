package be.cetic.tsaas.websocket

import java.util.UUID

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives.{JavaUUID, as, complete, delete, entity, get, handleWebSocketMessages, path, pathEnd, pathPrefix, post, _}
import akka.http.scaladsl.server.Route
import be.cetic.tsaas.datastream.{TimedIterator, TimedIteratorConfigJsonProtocol}
import be.cetic.tsaas.utils.json.UUIDJsonProtocol
import spray.json.DefaultJsonProtocol


object StreamActionSchema {

  case class Action(action: String)

}

class StreamOperation(wsFactory: WebsocketFactory) extends TimedIteratorConfigJsonProtocol with WebsocketActorJsonProtocol
  with UUIDJsonProtocol with DefaultJsonProtocol with SprayJsonSupport {


  val streamRoutes: Route = pathPrefix(JavaUUID) { wsId =>
    pathEnd(createOrUpdateConfig(wsId) ~ readConfig(wsId) ~ deleteConfig(wsId)) ~
      act(wsId) ~ websocket(wsId)
  }


  def websocket(wsId: UUID): Route = {
    path("ws") {
      handleWebSocketMessages {
        wsFactory.getOrCreateWebsocketHandler(wsId).flow
      }
    }
  }


  def createOrUpdateConfig(wsId: UUID): Route = {
    post {
      entity(as[TimedIterator.Config]) { config =>
        complete {
          wsFactory.createOrUpdateConfig(config, wsId)
        }
      }
    }
  }

  def readConfig(wsId: UUID): Route = {
    get {
      complete {
        wsFactory.readConfig(wsId)
      }
    }
  }

  def deleteConfig(wsId: UUID): Route = {
    delete {
      complete {
        wsFactory.deleteConfig(wsId)
        StatusCodes.NoContent
      }
    }
  }

  def act(wsId: UUID): Route = {
    path("act") {
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
}

