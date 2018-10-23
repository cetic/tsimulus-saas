package be.cetic.backend.websocket

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable, PoisonPill, Props}
import be.cetic.backend.websocket.WsSinkActor.{WsDropped, WsSinkTerminate, WsSource, WsTimeout}

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

object WsSinkActor {

  case class WsSource(ref: ActorRef)

  case object WsDropped

  case class WsSinkTerminate(wsId: UUID)

  case class WsTimeout(wsId: UUID)

  def props(timeToLive: FiniteDuration, wsId: UUID): Props = Props(new WsSinkActor(timeToLive, wsId))
}

class WsSinkActor(timeToLive: FiniteDuration, wsId: UUID) extends Actor with ActorLogging {
  implicit val scheduler: ExecutionContextExecutor = context.dispatcher
  var wsSourceActor: Option[ActorRef] = None

  log.info(s"Sink Actor Created, available for $timeToLive seconds.")

  var counter = 0

  override def postStop(): Unit = {
    context.parent ! WsSinkTerminate(wsId)
    super.postStop()
  }

  def receive: Actor.Receive = {
    case WsSource(wsSource) =>
      wsSourceActor = Some(wsSource)
      val count: Runnable = () => {
        wsSourceActor.get ! counter.toString
        counter += 1
      }
      context.system.scheduler.schedule(1 second, 5 seconds, count)


    case WsDropped =>
      log.info(s"Websocket connection closed. Scheduling dead in $timeToLive seconds")
  }


}
