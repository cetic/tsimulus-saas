package be.cetic.tsaas.datastream.tsimulus

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import be.cetic.tsaas.datastream.tsimulus.TsimulusIterator._
import be.cetic.rtsgen.config.Configuration
import spray.json.{DefaultJsonProtocol, JsNumber, JsObject, JsString, JsValue, RootJsonFormat, _}

trait TsimulusStreamConfigJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {

  implicit object SpeedJsonFormat extends RootJsonFormat[Speed] {
    def write(s: Speed): JsValue = {
      s match {
        case SpeedFactor(factor) => JsObject(Map("speed" -> JsNumber(factor)))
        case InfiniteSpeed => JsObject(Map("speed" -> JsString("inf")))
        case Realtime => JsObject(Map("speed"->JsString("realtime")))
        case x => throw DeserializationException(s"Unknown speed factor $x")
      }
    }

    def read(js: JsValue): Speed = {
      val jsMap = js.asJsObject.fields
      jsMap("speed") match {
        case JsNumber(value) => SpeedFactor(value.toFloat)
        case JsString(s) if s.replace("_", "").toLowerCase == "inf"  => InfiniteSpeed
        case JsString(s) if s.replace("_", "").toLowerCase == "infinite"  => InfiniteSpeed
        case JsString(s) if s.replace("_", "").toLowerCase == "infinity"  => InfiniteSpeed
        case JsString(s) if s.replace("_", "").toLowerCase == "realtime"  => Realtime
        case x => throw new DeserializationException(s"Unknown speed factor $x")
      }
    }
  }

  implicit val templateJsonFormat: RootJsonFormat[Template] = jsonFormat4(Template)
  implicit val templateMapJsonFormat: RootJsonFormat[TemplateMap] = jsonFormat2(TemplateMap)

  implicit object TsimulusConfigJsonFormat extends RootJsonFormat[TsimulusStreamConfig] {
    override def write(obj: TsimulusStreamConfig): JsValue = {
      val map = Map(
        "config" -> obj.config.toJson,
        "speed" -> obj.speed.toJson,
        "template" -> obj.template.toJson,
        "type" -> JsString("tsimulus")
      )
      JsObject(map)
    }

    override def read(json: JsValue): TsimulusStreamConfig = {
      val jsMap = json.asJsObject().fields
      val config = Configuration(jsMap("config"))
      val speed = if (jsMap.keys.toSeq.contains("speed")) json.convertTo[Speed] else SpeedFactor(1)
      jsMap.get("template")
        .map { template =>
          if (template.asJsObject.fields.keySet.diff(Set("template", "timeVariable", "nameVariable", "valueVariable")).isEmpty) {
            val template = jsMap.get("template").map(_.convertTo[Template]).getOrElse(defaultTemplate)
            TsimulusStreamConfig(config, speed, template)
          }
          else {
            val template = jsMap("template").convertTo[Seq[TemplateMap]]
            TsimulusStreamConfig(config, speed, template)
          }
        }
        .getOrElse(TsimulusStreamConfig(config, speed))
    }
  }

}
