package be.cetic.tsaas.websocket

import java.util.UUID

import akka.NotUsed
import akka.pattern.ask
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.{Materializer, OverflowStrategy}
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, Sink, Source}
import akka.util.Timeout
import be.cetic.tsaas.datastream.TimedIterator
import be.cetic.tsaas.datastream.TimedIterator.Config
import be.cetic.tsaas.websocket.WebsocketActor.{EmptyStreamConfiguration, StreamingConfirmation, StreamingNotStarted, StreamingStarted}
import be.cetic.tsaas.websocket.WebsocketFactory.WsHandler

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}

object WebsocketFactory {

  case class CreateWsEntry(timeToLive: FiniteDuration)

  case class GetWsFlow(wsId: UUID)

  case object WsUnavailable

  case class WsHandler(websocketActor: ActorRef, streamEntry: ActorRef, flow: Flow[Message, Message, NotUsed])

}

class WebsocketFactory(implicit val system: ActorSystem, implicit val materializer: Materializer) {
  implicit val dispatcher: ExecutionContextExecutor = system.dispatcher
  implicit val timeout: Timeout = Timeout(20 seconds)
  private var configCache: Map[UUID, TimedIterator.Config] = Map()
  private var wsHandlers: Map[UUID, WsHandler] = Map()


  def createOrUpdateConfig(wsConfig: TimedIterator.Config): UUID = {
    val wsId = UUID.randomUUID()
    createOrUpdateConfig(wsConfig, wsId)
  }

  def createOrUpdateConfig(wsConfig: TimedIterator.Config, wsId: UUID): UUID = {
    configCache = configCache.updated(wsId, wsConfig)
    getOrCreateWebsocketHandler(wsId).websocketActor ! WebsocketActor.Configure(wsConfig)
    wsId
  }

  def readConfigs(): Map[UUID, Config] = {
    configCache.map { case (wsid, conf) => wsid -> conf }
  }

  def readConfig(wsId: UUID): Map[UUID, Config] = {
    configCache.get(wsId).map(c=>Map(wsId -> c)).getOrElse(Map())
  }

  def deleteConfig(wsId: UUID): Unit = {
    wsHandlers.get(wsId).foreach { handler =>
      system.stop(handler.websocketActor)
      system.stop(handler.streamEntry)
    }
    configCache -= wsId
  }

  def startStream(wsId: UUID): Future[String] = {
    (getOrCreateWebsocketHandler(wsId).websocketActor ? WebsocketActor.Start).mapTo[StreamingConfirmation]
      .map {
        case StreamingStarted => "Streaming started"
        case StreamingNotStarted(t) => s"Streaming could not start because of ${t.getMessage}"
        case EmptyStreamConfiguration => "Missing stream configuration"
      }
  }

  def stopStream(wsId: UUID): String = {
    configCache.get(wsId).map {
      config =>
        getOrCreateWebsocketHandler(wsId).websocketActor ! WebsocketActor.Stop
        "Stream stopped"
    }.getOrElse("Stream not configured.")
  }

  def streamStatus(wsId: UUID): Future[WebsocketActor.Status] = {
    (getOrCreateWebsocketHandler(wsId).websocketActor ? WebsocketActor.StatusRequest).mapTo[WebsocketActor.Status]
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
