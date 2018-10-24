package be.cetic.backend.websocket

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable, Props}
import akka.http.scaladsl.model.ws.TextMessage
import be.cetic.backend.datastream.TimedIterator
import be.cetic.backend.websocket.WebsocketActor._

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.FiniteDuration

object WebsocketActor {

  case class WsSource(ref: ActorRef)

  case object WsDropped

  case class Configure(config: TimedIterator.Config)

  case object Start

  case object Stop

  case object StatusRequest

  case class Status(running: Boolean, nextDelay: Option[Int])

  trait StreamingConfirmation

  case object EmptyStreamConfiguration extends StreamingConfirmation

  case object StreamingStarted extends StreamingConfirmation

  case object StreamingNotStarted extends StreamingConfirmation

  def props(wsSourceActor: ActorRef): Props =
    Props(new WebsocketActor(wsSourceActor))
}

class WebsocketActor[T](wsSourceActor: ActorRef) extends Actor with ActorLogging {
  implicit val scheduler: ExecutionContextExecutor = context.dispatcher

  private var iteratorSchedule: Cancellable = Cancellable.alreadyCancelled

  private var emptyIterator: Boolean = false

  private var streamConfig: Option[TimedIterator.Config] = None

  private var maybeNextDelay: Option[FiniteDuration] = None


  def receive: Actor.Receive = {
    case Configure(config) => streamConfig = Some(config)

    case Start => sender ! consumeAndConfirm(sender)

    case Stop => iteratorSchedule.cancel()

    case WsDropped => log.info(s"Websocket connection closed.")

    case TextMessage.Strict("help") => wsSourceActor ! s"""commands:[empty, restart, configured, stop, running]"""

    case TextMessage.Strict("empty") => wsSourceActor ! s"""{"empty":$emptyIterator}"""

    case TextMessage.Strict("restart") => wsSourceActor ! s"""{"restart":${consume()}}"""

    case TextMessage.Strict("configured") => wsSourceActor ! s"""{"configured":${streamConfig.nonEmpty}}"""

    case TextMessage.Strict("stop") => wsSourceActor ! s"""{"cancel":${stop()}}"""

    case TextMessage.Strict("running") => wsSourceActor ! s"""{"running":${!maybeNextDelay.nonEmpty}}"""

    case TextMessage.Strict("delay") => wsSourceActor ! s"""{"delay":${maybeNextDelay.getOrElse(-1)}}"""

    case TextMessage.Strict(text) => wsSourceActor ! s"""{"echo":$text}"""

    case msg => log.info(s"Received unhandled message $msg")
  }

  def consumeAndConfirm(confirmTo: ActorRef): Unit = {
    if (streamConfig.isEmpty) {
      confirmTo ! EmptyStreamConfiguration
      return
    }
    confirmTo ! (if (consume()) StreamingStarted else StreamingNotStarted)
  }

  def stop(): Boolean = {
    maybeNextDelay = None
    iteratorSchedule.cancel()
  }

  def consume(): Boolean = {
    streamConfig.exists { config =>
      val iterator = TimedIterator.factory[String](config)
      emptyIterator = false
      runAndSchedule(iterator)
      true
    }
  }

  def runAndSchedule(timedIterator: TimedIterator[String]): Unit = {
    iteratorSchedule.cancel()
    val nextRound = timedIterator.next()
      .exists { case (delay, data) =>
        maybeNextDelay = Some(delay)
        wsSourceActor ! data
        val nextRun: Runnable = () => runAndSchedule(timedIterator)
        iteratorSchedule = context.system.scheduler.scheduleOnce(delay, nextRun)
        true
      }
    emptyIterator = !nextRound
    if (emptyIterator) stop()
  }

}
