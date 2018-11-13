package be.cetic.tsaas.websocket

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout
import be.cetic.tsaas.datastream.TimedIteratorConfigJsonProtocol
import be.cetic.tsaas.utils.json.UUIDJsonProtocol
import io.swagger.v3.oas.annotations.Operation
import javax.ws.rs.{GET, Path}
import spray.json._

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

@Path("stream")
trait WebSocketService extends TimedIteratorConfigJsonProtocol with WebsocketActorJsonProtocol with UUIDJsonProtocol with DefaultJsonProtocol with SprayJsonSupport {
  implicit val materializer: ActorMaterializer
  implicit val system: ActorSystem
  implicit val dispatcher: ExecutionContextExecutor = system.dispatcher
  implicit val timeout: Timeout = Timeout(20.seconds)
  val wsFactory = new WebsocketFactory

  val streamOp = new StreamOperation(wsFactory)

  val webSocketRoute: Route =
    pathPrefix("stream") {
      pathEnd(readConfigs()) ~ streamOp.streamRoutes
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

}


