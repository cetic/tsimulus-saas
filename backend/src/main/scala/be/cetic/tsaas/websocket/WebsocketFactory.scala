package be.cetic.tsaas.websocket

import java.util.UUID

import akka.NotUsed
import akka.pattern.ask
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.{Materializer, OverflowStrategy}
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, Sink, Source}
import akka.util.Timeout
import be.cetic.tsaas.datastream.TimedIterator
import be.cetic.tsaas.datastream.TimedIterator.StreamConfig
import be.cetic.tsaas.websocket.WebsocketActor.{EmptyStreamConfiguration, StreamingConfirmation, StreamingNotStarted, StreamingStarted}
import be.cetic.tsaas.websocket.WebsocketFactory.{WebsocketConfig, WsHandler}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}
import spray.json._

object WebsocketFactory {

  case class CreateWsEntry(timeToLive: FiniteDuration)

  case class GetWsFlow(wsId: UUID)

  case object WsUnavailable

  case class WsHandler(websocketActor: ActorRef, streamEntry: ActorRef, flow: Flow[Message, Message, NotUsed])

  case class WebsocketConfig(streamId: UUID, streamConfig: StreamConfig)

}

class WebsocketFactory(implicit val system: ActorSystem, implicit val materializer: Materializer) extends WebsocketActorJsonProtocol{
  implicit val dispatcher: ExecutionContextExecutor = system.dispatcher
  implicit val timeout: Timeout = Timeout(20 seconds)
  private var configCache: Map[UUID, TimedIterator.StreamConfig] = Map()
  private var wsHandlers: Map[UUID, WsHandler] = Map()


  def createOrUpdateConfig(wsConfig: TimedIterator.StreamConfig): WebsocketConfig = {
    val wsId = UUID.randomUUID()
    createOrUpdateConfig(wsConfig, wsId)
  }

  def createOrUpdateConfig(wsConfig: TimedIterator.StreamConfig, wsId: UUID): WebsocketConfig = {
    configCache = configCache.updated(wsId, wsConfig)
    getOrCreateWebsocketHandler(wsId).websocketActor ! WebsocketActor.Configure(wsConfig)
    WebsocketConfig(wsId, wsConfig)
  }

  def readConfigs(): Seq[WebsocketConfig] = {
    configCache.map { case (wsId, conf) => WebsocketConfig(wsId, conf) }.toSeq
  }

  def readConfig(wsId: UUID): Option[WebsocketConfig] = {
    configCache.get(wsId).map(c => WebsocketConfig(wsId, c))
  }

  def deleteConfig(wsId: UUID): Unit = {
    wsHandlers.get(wsId).foreach { handler =>
      system.stop(handler.websocketActor)
      system.stop(handler.streamEntry)
    }
    configCache -= wsId
    wsHandlers -= wsId
  }

  def startStream(wsId: UUID, once: Boolean = false): Future[HttpResponse] = {
    val msg = if (once) WebsocketActor.Validate else WebsocketActor.Start
    (getOrCreateWebsocketHandler(wsId).websocketActor ? msg).mapTo[StreamingConfirmation]
      .map {
        case StreamingStarted => HttpResponse(StatusCodes.Accepted,
          entity = s"""{"msg":Streaming ${if (once) "valid" else "started"}}"""")
        case StreamingNotStarted(t) => HttpResponse(StatusCodes.InternalServerError,
          entity = s"""{"msg":"Streaming could not start because of ${t.getMessage}}"""")
        case EmptyStreamConfiguration => HttpResponse(StatusCodes.RetryWith,
          entity = """{"msg":"Missing stream configuration. Retry after posting a configuration"}""")
      }
  }

  def stopStream(wsId: UUID): HttpResponse = {
    configCache.get(wsId).map {
      config =>
        getOrCreateWebsocketHandler(wsId).websocketActor ! WebsocketActor.Stop
        HttpResponse(StatusCodes.OK, entity = """{"msg":"Stream stopped"}""")
    }.getOrElse {
      HttpResponse(StatusCodes.RetryWith, entity = """{"msg":"Stream not configured.}"""")
    }
  }

  def streamStatus(wsId: UUID): Future[HttpResponse] = {
    (getOrCreateWebsocketHandler(wsId).websocketActor ? WebsocketActor.StatusRequest).mapTo[WebsocketActor.Status]
    .map(status => HttpResponse(StatusCodes.OK, entity = status.toJson.toString))
  }

  def getOrCreateWebsocketHandler(wsId: UUID): WsHandler = {
    wsHandlers.getOrElse(wsId, createWebsocketHandler(wsId))
  }

  def createWebsocketHandler(wsId: UUID): WsHandler = {
    val source: Source[Message, ActorRef] =
      Source.actorRef(bufferSize = 1024, overflowStrategy = OverflowStrategy.dropHead)
        .map((s: String) => TextMessage.Strict(s))
        .keepAlive(maxIdle = 10.seconds, () => TextMessage.Strict("Keep-alive message sent to WebSocket recipient"))

    val (streamEntry: ActorRef, messageSource: Source[Message, NotUsed]) = source.toMat(BroadcastHub.sink(1024))(Keep.both).run

    val websocketActor: ActorRef = system.actorOf(WebsocketActor.props(streamEntry))
    val sink: Sink[Message, NotUsed] = Flow[Message].to(Sink.actorRef(websocketActor, WebsocketActor.WsDropped))

    val flow = Flow.fromSinkAndSource(sink, messageSource)

    wsHandlers = wsHandlers.updated(wsId, WsHandler(websocketActor, streamEntry, flow))
    wsHandlers(wsId)
  }
}
