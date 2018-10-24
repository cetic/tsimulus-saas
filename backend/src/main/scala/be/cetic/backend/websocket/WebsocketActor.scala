package be.cetic.backend.websocket

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable, Props}
import akka.http.scaladsl.model.ws.TextMessage
import be.cetic.backend.datastream.TimedIterator
import be.cetic.backend.websocket.WebsocketActor._

import scala.concurrent.ExecutionContextExecutor

object WebsocketActor {

  case class WsSource(ref: ActorRef)

  case object WsDropped

  case class WsSinkTerminate(wsId: UUID)

  case class WsTimeout(wsId: UUID)

  case class WsConfig(config: Any, description: String = "")

  case class Consume(timedIterator: TimedIterator[String])

  def props(wsSourceActor: ActorRef): Props =
    Props(new WebsocketActor(wsSourceActor))
}

class WebsocketActor[T](wsSourceActor: ActorRef) extends Actor with ActorLogging {
  implicit val scheduler: ExecutionContextExecutor = context.dispatcher

  private var iteratorSchedule: Cancellable = Cancellable.alreadyCancelled

  private var emptyIterator: Boolean = false


  def receive: Actor.Receive = {
    case Consume(timedIterator) => runAndSchedule(timedIterator)

    case WsDropped => log.info(s"Websocket connection closed.")

    case t@TextMessage.Strict(text) => wsSourceActor ! text

    case msg => log.info(s"Received message $msg")
  }


  def runAndSchedule(timedIterator: TimedIterator[String]): Unit = {
    iteratorSchedule.cancel()
    val nextRound = timedIterator.next()
      .exists { case (delay, data) =>
        wsSourceActor ! data
        val nextRun: Runnable = () => runAndSchedule(timedIterator)
        iteratorSchedule = context.system.scheduler.scheduleOnce(delay, nextRun)
        true
      }
    emptyIterator = !nextRound
  }

}
