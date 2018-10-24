package be.cetic.backend.websocket

import java.util.UUID

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, Sink, Source}
import akka.stream.{Materializer, OverflowStrategy}
import be.cetic.backend.datastream.TimedIterator
import be.cetic.backend.websocket.WebsocketFactory.WsHandler

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

object WebsocketFactory {

  case class CreateWsEntry(timeToLive: FiniteDuration)

  case class GetWsFlow(wsId: UUID)

  case object WsUnavailable


  case class WsHandler(websocketActor: ActorRef, streamEntry: ActorRef, flow: Flow[Message, Message, NotUsed])

}

class WebsocketFactory(implicit val system: ActorSystem, implicit val materializer: Materializer) {
  implicit val dispatcher: ExecutionContextExecutor = system.dispatcher
  private var configCache: Map[UUID, TimedIterator.Config] = Map()
  private var wsHandlers: Map[UUID, WsHandler] = Map()


  def createOrUpdateConfig(wsConfig: TimedIterator.Config): UUID = {
    val wsId = UUID.randomUUID()
    createOrUpdateConfig(wsConfig, wsId)
  }

  def createOrUpdateConfig(wsConfig: TimedIterator.Config, wsId: UUID): UUID = {
    deleteConfig(wsId)
    configCache = configCache.updated(wsId, wsConfig)
    wsId
  }

  def readConfigs(): Map[UUID, String] = {
    configCache.map { case (wsid, conf) => wsid -> conf.description }
  }

  def readConfig(wsId: UUID): String = {
    configCache.get(wsId).map(_.description).getOrElse("No such resource.")
  }

  def deleteConfig(wsId: UUID): Unit = {
    wsHandlers.get(wsId).foreach { handler =>
      system.stop(handler.websocketActor)
      system.stop(handler.streamEntry)
    }
    configCache -= wsId
  }

  def startStream(wsId: UUID): String=  {
    configCache.get(wsId).map{config=>
      getOrCreateWebsocketHandler(wsId).websocketActor ! WebsocketActor.Consume(TimedIterator.factory(config))
      "Stream started"
    }.getOrElse("Stream not configured.")
  }

  def getOrCreateWebsocketHandler(wsId: UUID): WsHandler ={
    wsHandlers.getOrElse(wsId, createWebsocketHandler(wsId))
  }

  def createWebsocketHandler(wsId: UUID): WsHandler = {
    val source: Source[Message, ActorRef] =
      Source.actorRef(bufferSize = 1024, overflowStrategy = OverflowStrategy.dropHead)
        .map((s: String) => TextMessage.Strict(s))
        .keepAlive(maxIdle = 10.seconds, () => TextMessage.Strict("Keep-alive message sent to WebSocket recipient"))

    val (streamEntry: ActorRef, messageSource: Source[Message, NotUsed]) = source.toMat(BroadcastHub.sink(1024))(Keep.both).run

    val websocketActor : ActorRef = system.actorOf(WebsocketActor.props(streamEntry))
    val sink: Sink[Message, NotUsed] = Flow[Message].to(Sink.actorRef(websocketActor, WebsocketActor.WsDropped))

    val flow = Flow.fromSinkAndSource(sink, messageSource)

    wsHandlers = wsHandlers.updated(wsId, WsHandler(websocketActor, streamEntry, flow))
    wsHandlers(wsId)
  }
}
