package be.cetic.tsaas.websocket

import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable, Props}
import akka.http.scaladsl.model.ws.TextMessage
import be.cetic.tsaas.datastream.TimedIterator
import be.cetic.tsaas.websocket.WebsocketActor._

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.FiniteDuration

object WebsocketActor {

  case object WsDropped

  case class Configure(config: TimedIterator.Config)

  trait Operation

  case object Validate extends Operation

  case object Start extends Operation

  case object Stop extends Operation

  case object StatusRequest extends Operation

  case class Status(running: Boolean, configured: Boolean, nextDelay: Option[Int])

  trait StreamingConfirmation

  case object EmptyStreamConfiguration extends StreamingConfirmation

  case object StreamingStarted extends StreamingConfirmation

  case class StreamingNotStarted(t: Throwable) extends StreamingConfirmation

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

    case Validate =>sender ! consumeAndConfirm(sender, once=true)

    case Start => sender ! consumeAndConfirm(sender)

    case Stop => iteratorSchedule.cancel()

    case StatusRequest => sender ! Status(!iteratorSchedule.isCancelled, streamConfig.nonEmpty, maybeNextDelay.map(_.length.toInt))

    case WsDropped => log.info(s"Websocket connection closed.")

    case TextMessage.Strict("help") => wsSourceActor ! s"""commands:[empty, restart, configured, stop, running]"""

    case TextMessage.Strict("empty") => wsSourceActor ! s"""{"empty":${emptyIterator && streamConfig.nonEmpty} }"""

    case TextMessage.Strict("restart") => wsSourceActor ! s"""{"restart":${consume()}}"""

    case TextMessage.Strict("configured") => wsSourceActor ! s"""{"configured":${streamConfig.nonEmpty}}"""

    case TextMessage.Strict("stop") => wsSourceActor ! s"""{"cancel":${stop()}}"""

    case TextMessage.Strict("running") => wsSourceActor ! s"""{"running":${maybeNextDelay.isEmpty}}"""

    case TextMessage.Strict("delay") => wsSourceActor ! s"""{"delay":${maybeNextDelay.getOrElse(-1)}}"""

    case TextMessage.Strict(text) => wsSourceActor ! s"""{"echo":$text}"""

    case msg => log.info(s"Received unhandled message $msg")
  }

  def consumeAndConfirm(confirmTo: ActorRef, once: Boolean=false): Unit = {
    if (streamConfig.isEmpty) {
      confirmTo ! EmptyStreamConfiguration
      return
    }
    confirmTo ! {
      try {
        consume(once)
        StreamingStarted
      }
      catch {
        case t: Throwable =>
          StreamingNotStarted(t)
      }
    }
  }

  def stop(): Boolean = {
    maybeNextDelay = None
    iteratorSchedule.cancel()
  }

  def consume(once: Boolean= false): Boolean = {
    streamConfig.exists { config =>
      val iterator = TimedIterator.factory[String](config)
      emptyIterator = false
      if (!once) runAndSchedule(iterator)
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
