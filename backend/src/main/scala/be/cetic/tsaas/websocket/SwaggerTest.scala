package be.cetic.tsaas.websocket

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.github.swagger.akka.SwaggerHttpService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.{Content, Schema}
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.models.ExternalDocumentation


import javax.ws.rs.{GET, Path}

@Path("/root")
class SwaggerTest {

  @GET
  def getString(s: String) = get(complete(s))

  @Path("/sub")
  @Operation()
  @GET
  def subPath() = path("sub")(getString("sub"))



  def swRoutesGen() = pathPrefix("root") {
    pathEnd {
      getString("root") ~ subPath()
    }
  }


  val swRoutes: Route = swRoutesGen()


}

object SwaggerDocService extends SwaggerHttpService {
  override val apiClasses = Set(classOf[WebSocketService])
  override val host = "localhost:8080"


  override val externalDocs = Some(new ExternalDocumentation().description("Core Docs").url("http://acme.com/docs"))

  //override val securitySchemeDefinitions = Map("basicAuth" -> new BasicAuthDefinition())
  override val unwantedDefinitions = Seq("Function1", "Function1RequestContextFutureRouteResult")
}