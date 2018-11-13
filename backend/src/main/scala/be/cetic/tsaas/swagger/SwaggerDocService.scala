package be.cetic.tsaas.swagger

import be.cetic.tsaas.websocket.{StreamOperation, WebSocketService}
import com.github.swagger.akka.SwaggerHttpService
import io.swagger.v3.oas.models.ExternalDocumentation

object SwaggerDocService extends SwaggerHttpService {
  override val apiClasses = Set(classOf[WebSocketService], classOf[StreamOperation])
  override val host = "localhost:8080"


  override val externalDocs = Some(new ExternalDocumentation().description("Core Docs").url("http://acme.com/docs"))

  //override val securitySchemeDefinitions = Map("basicAuth" -> new BasicAuthDefinition())
  override val unwantedDefinitions = Seq("Function1", "Function1RequestContextFutureRouteResult", "Function1RequestContextFutureRouteResult")
}
