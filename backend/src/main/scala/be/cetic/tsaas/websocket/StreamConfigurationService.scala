package be.cetic.tsaas.websocket

import java.util.UUID

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives.{as, complete, delete, entity, get, post}
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import be.cetic.tsaas.datastream.{TimedIterator, TimedIteratorConfigJsonProtocol}
import be.cetic.tsaas.utils.json.UUIDJsonProtocol
import spray.json.DefaultJsonProtocol
import spray.json._
import scala.concurrent.ExecutionContextExecutor

trait StreamConfigurationService extends TimedIteratorConfigJsonProtocol with WebsocketActorJsonProtocol with UUIDJsonProtocol with DefaultJsonProtocol with SprayJsonSupport {
  implicit val materializer: ActorMaterializer
  implicit val system: ActorSystem
  implicit val dispatcher: ExecutionContextExecutor

  val wsFactory: WebsocketFactory


  def readConfigs() = {
    get {
      complete {
        wsFactory.readConfigs()
      }
    }
  }

  def postConfigs() = {
    val wsId = UUID.randomUUID()
    createOrUpdateConfig(wsId)
  }


  def createOrUpdateConfig(wsId: UUID): Route = {
    post {
      entity(as[TimedIterator.StreamConfig]) { config =>
        complete {
          wsFactory.createOrUpdateConfig(config, wsId)
        }
      }
    }
  }

  def readConfig(wsId: UUID): Route = {
    get {
      complete {
        val response= wsFactory.readConfig(wsId)
          .map(conf =>HttpResponse(StatusCodes.OK, entity=conf.toJson.toString))
          .getOrElse(HttpResponse(StatusCodes.NoContent))
        response
      }
    }
  }

  def deleteConfig(wsId: UUID): Route = {
    delete {
      complete {
        wsFactory.deleteConfig(wsId)
        StatusCodes.NoContent
      }
    }
  }
}
