package be.cetic.tsaas.websocket

import java.util.UUID

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives.{JavaUUID, as, complete, delete, entity, get, handleWebSocketMessages, path, pathEnd, pathPrefix, post, _}
import akka.http.scaladsl.server.Route
import be.cetic.tsaas.datastream.{TimedIterator, TimedIteratorConfigJsonProtocol}
import be.cetic.tsaas.utils.json.UUIDJsonProtocol
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.{Content, Schema}
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.{Operation, Parameter}
import javax.ws.rs._
import spray.json.DefaultJsonProtocol


object StreamActionSchema {

  case class Action(@Schema(required = true, `type` = "string", allowableValues = Array("start", "stop", "validate", "status")) action: String)

}

@Path("stream/{wsId}")
class StreamOperation(wsFactory: WebsocketFactory) extends TimedIteratorConfigJsonProtocol with WebsocketActorJsonProtocol with UUIDJsonProtocol with DefaultJsonProtocol with SprayJsonSupport {


  val streamRoutes: Route = pathPrefix(JavaUUID) { wsId =>
    pathEnd(createOrUpdateConfig(wsId) ~ readConfig(wsId) ~ deleteConfig(wsId)) ~
      act(wsId)
  }


  def websocket(wsId: UUID) = {
    path("ws") {
      handleWebSocketMessages {
        wsFactory.getOrCreateWebsocketHandler(wsId).flow
      }
    }
  }

  @Operation(description = "Post a configuration to the ws with given uuid")
  @POST
  def createOrUpdateConfig(@Parameter(name = "wsId", in = ParameterIn.PATH, description = "UUID of the websocket") wsId: UUID) = {
    post {
      entity(as[TimedIterator.Config]) { config =>
        complete {
          wsFactory.createOrUpdateConfig(config, wsId)
        }
      }
    }
  }

  @Operation(description = "Read the configuration of the ws with given uuid")
  @GET
  def readConfig(@Parameter(name = "wsId", in = ParameterIn.PATH, description = "UUID of the websocket") wsId: UUID) = {
    get {
      complete {
        wsFactory.readConfig(wsId)
      }
    }
  }

  @Operation(description = "Delete the configuration of ws with given uuid")
  @DELETE
  def deleteConfig(@Parameter(name = "wsId", in = ParameterIn.PATH, description = "UUID of the websocket") wsId: UUID) = {
    delete {
      complete {
        wsFactory.deleteConfig(wsId)
        s"Configuration $wsId deleted."
      }
    }
  }

  @Path("/act")
  @Operation(description = "Send an action to the websocket handler.",
    requestBody = new RequestBody(content = Array(new Content(schema = new Schema(implementation = classOf[StreamActionSchema.Action])))))
  @POST
  def act( @Parameter(name = "wsId", in = ParameterIn.PATH, description = "UUID of the websocket") wsId: UUID) = {
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

