package be.cetic.backend

import akka.actor._
import com.typesafe.config.ConfigFactory
import backend.tsimulus._
import be.cetic.backend.tsimulus.TsimulusBackend

import scala.collection.JavaConversions._
import scala.concurrent.Await
import scala.concurrent.duration.Duration
/**
 * Booting a cluster backend node with all actors
 */
object Backend extends App {

  // Simple cli parsing
  val port = args match {
    case Array()     => "2551"
    case Array(port) => port
    case args        => throw new IllegalArgumentException(s"only ports. Args [ $args ] are invalid")
  }

  // System initialization
  val properties = Map(
      "akka.remote.netty.tcp.port" -> port
  )
  
  val system = ActorSystem("application", (ConfigFactory parseMap properties)
    .withFallback(ConfigFactory.load())
  )
   
  // Deploy actors and services
  TsimulusBackend startOn system

  Await.result(system.whenTerminated, Duration.Inf)
}