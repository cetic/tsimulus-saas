package be.cetic.tsaas.websocket

import java.util.UUID

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives.{handleWebSocketMessages, path, _}
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout
import be.cetic.tsaas.datastream.{TimedIterator, TimedIteratorConfigJsonProtocol}
import be.cetic.tsaas.utils.json.UUIDJsonProtocol
import io.swagger.v3.oas.annotations.Operation
import javax.ws.rs.{DELETE, GET, POST, Path}
import spray.json._

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

@Path("stream")
trait WebSocketService extends TimedIteratorConfigJsonProtocol with WebsocketActorJsonProtocol with UUIDJsonProtocol with DefaultJsonProtocol with SprayJsonSupport {
  implicit val materializer: ActorMaterializer
  implicit val system: ActorSystem
  implicit val dispatcher: ExecutionContextExecutor = system.dispatcher
  implicit val timeout: Timeout = Timeout(20.seconds)
  private val wsFactory = new WebsocketFactory


  val webSocketRoute: Route =
    pathPrefix("stream") {
      pathEnd {
        readConfigs()
      } ~
        pathPrefix(JavaUUID) { wsId =>
          pathEnd {
            createOrUpdateConfig(wsId) ~ readConfig(wsId) ~ deleteConfig(wsId)
          } ~
            path("validate") {
              validate(wsId)
            } ~
            path("status") {
              status(wsId)
            } ~
            path("start") {
              start(wsId)
            } ~
            path("stop") {
              stop(wsId)
            } ~
            path("ws") {
              handleWebSocketMessages {
                wsFactory.getOrCreateWebsocketHandler(wsId).flow
              }
            }
        }
    }


  @Operation(description = "Provides all stored configurations")
  @GET
  def readConfigs() = {
    get {
      complete {
        wsFactory.readConfigs()
      }
    }
  }


  @Path("{UUID}")
  @Operation(description = "Post a configuration to the ws with given uuid")
  @POST
  def createOrUpdateConfig(wsId: UUID) = {
    post {
      entity(as[TimedIterator.Config]) { config =>
        complete {
          wsFactory.createOrUpdateConfig(config, wsId)
        }
      }
    }
  }


  @Path("{UUID}")
  @Operation(description = "Read the configuration of the ws with given uuid")
  @GET
  def readConfig(wsId: UUID) = {
    get {
      complete {
        wsFactory.readConfig(wsId)
      }
    }
  }

  @Path("{UUID}")
  @Operation(description = "Delete the configuration of ws with given uuid")
  @DELETE
  def deleteConfig(wsId: UUID) = {
    delete {
      complete {
        wsFactory.deleteConfig(wsId)
        s"Configuration $wsId deleted."
      }
    }
  }

  @Path("{UUID}/validate")
  @Operation(description = "Validate the configuration.")
  @GET
  def validate(wsId: UUID) = {
    get {
      complete {
        wsFactory.startStream(wsId, once = true)
      }
    }
  }

  @Path("{UUID}/start")
  @Operation(description = "Start the stream.")
  @POST
  def start(wsId: UUID) = {
    get {
      complete {
        wsFactory.startStream(wsId)
      }
    }
  }

  @Path("{UUID}/stop")
  @Operation(description = "Stop the stream.")
  @POST
  def stop(wsId: UUID) = {
    get {
      complete {
        wsFactory.stopStream(wsId)
      }
    }
  }

  @Path("{UUID}/status")
  @Operation(description = "Websocket status.")
  @GET
  def status(wsId: UUID) = {
    get {
      complete {
        wsFactory.streamStatus(wsId)
      }
    }
  }

}


