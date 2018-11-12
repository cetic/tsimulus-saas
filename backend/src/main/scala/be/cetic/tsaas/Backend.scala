package be.cetic.tsaas

import akka.actor._
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn


object Backend extends App {

  // Simple cli parsing
  val port = args match {
    case Array() => 8080
    case Array(port) => port.toInt
    case args => throw new IllegalArgumentException(s"only ports. Args [ $args ] are invalid")
  }

  implicit val system = ActorSystem("TSaaS-Backend")
  implicit val materializer: ActorMaterializer =  ActorMaterializer()
  implicit val dispatcher : ExecutionContextExecutor = system.dispatcher
  val routes: Route = new TSaaService().routes
  //val bindingInterface = ${?BINDING}


  val bindingFuture = Http().bindAndHandle(routes, "0.0.0.0", port)
  println(s"Server online at http://localhost:$port/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done



}