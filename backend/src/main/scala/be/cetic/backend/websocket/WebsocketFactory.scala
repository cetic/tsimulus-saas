package be.cetic.backend.websocket

import java.util.UUID

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.stream.{Materializer, OverflowStrategy}
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, Sink, Source}


object WebsocketFactory {

  def generateSource[T](implicit fm: Materializer, system: ActorSystem): (ActorRef, Source[T, NotUsed]) = {

    val source: Source[T, ActorRef] = Source.actorRef[T](1000, OverflowStrategy.dropHead)

    val (streamEntry: ActorRef, publisherSource: Source[T, NotUsed]) = source.toMat(BroadcastHub.sink(bufferSize = 1024))(Keep.both).run

    (streamEntry, publisherSource)
  }
}

class WebsocketFactory[T](serializer: T => String)(implicit fm: Materializer, system: ActorSystem) {

  val (streamEntry: ActorRef, publisherSource: Source[T, NotUsed]) = WebsocketFactory.generateSource[T]

  def webSocketFlow: Flow[Message, Message, NotUsed] =
    Flow[Message]
      .collect { case TextMessage.Strict(msg) => msg }
      .via(logicFlow)
      .map { msg: String => TextMessage.Strict(msg) }

  private def logicFlow: Flow[String, String, NotUsed] = Flow.fromSinkAndSource(Sink.ignore, publisherSource).map(t => serializer(t))

}
