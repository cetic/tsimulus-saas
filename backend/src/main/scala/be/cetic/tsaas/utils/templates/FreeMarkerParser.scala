package be.cetic.tsaas.utils.templates

import java.io.StringWriter

import be.cetic.tsaas.utils.templates.wrapper.{Base64WrapperMethod, NoiseCreatorWrapperMethod, ReplicatorWrapperMethod}
import freemarker.template.Template

import scala.collection.JavaConverters._


class FreeMarkerParser(templateString: String) {

  val temp: Template = new Template("template", templateString, FreeMarkerConfiguration.cfg)
  val buf = new StringWriter()
  val utilMethods = Map(
    "b64" -> new Base64WrapperMethod(),
    "noise" -> new NoiseCreatorWrapperMethod(),
    "replicate" -> new ReplicatorWrapperMethod())

  def process(data: Map[String, Any] = Map()): String = {
    temp.process((utilMethods ++ data).asJava, buf)
    val result = buf.toString
    buf.flush()
    result
  }
}
