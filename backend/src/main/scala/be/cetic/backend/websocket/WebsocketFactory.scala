package be.cetic.backend.websocket

import java.util.UUID

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem, Cancellable}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Sink, Source}

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

object WebsocketFactory {

  case class CreateWsEntry(timeToLive: FiniteDuration)

  case class GetWsFlow(wsId: UUID)

  case object WsUnavailable

}

class WebsocketFactory(implicit val system: ActorSystem) {
  implicit val dispatcher: ExecutionContextExecutor = system.dispatcher
  var sinkActors: Map[UUID, ActorRef] = Map()
  var deathPacts: Map[UUID, Cancellable] = Map()

  private def wsDeathPact(timeToLive: FiniteDuration, wsId: UUID): Cancellable = {
    val killWs: Runnable = () => {
      println(s"Killing WS $wsId")
      system.stop(sinkActors(wsId))}
    system.scheduler.scheduleOnce(timeToLive, killWs)
  }

  private def cancelDeathPact(wsId: UUID): Unit = {
    deathPacts.get(wsId).foreach { pact =>
      pact.cancel()
      println(s"Cancelled death pact for ws $wsId")
      //TODO : include log.
    }
  }

  def createWsEntry(timeToLive: FiniteDuration): UUID = {
    val wsId = UUID.randomUUID()
    val sinkActor = system.actorOf(WsSinkActor.props(timeToLive, wsId))
    sinkActors = sinkActors.updated(wsId, sinkActor)
    deathPacts = deathPacts.updated(wsId, wsDeathPact(timeToLive, wsId))
    wsId
  }

  def makeWsFlow(wsId: UUID): Option[Flow[Message, Message, NotUsed]] = {
    sinkActors.get(wsId)
      .map { sinkActor =>
        cancelDeathPact(wsId)

        val sink: Sink[Message, NotUsed] = Flow[Message].to(Sink.actorRef(sinkActor, WsSinkActor.WsDropped))

        val source: Source[Message, NotUsed] =
          Source.actorRef(bufferSize = 1000, overflowStrategy = OverflowStrategy.dropHead)
            .map((s: String) => TextMessage.Strict(s))
            .mapMaterializedValue { wsSource =>
              sinkActor ! WsSinkActor.WsSource(wsSource)
              NotUsed
            }
            .keepAlive(maxIdle = 10.seconds, () => TextMessage.Strict("Keep-alive message sent to WebSocket recipient"))
        Flow.fromSinkAndSource(sink, source)
      }

  }
}
