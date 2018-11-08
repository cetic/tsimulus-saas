package be.cetic.tsaas.utils.templates.wrapper

import freemarker.ext.beans.StringModel
import freemarker.template.{SimpleNumber, SimpleScalar}

object Parsers {

  def parseStringModel(sm: StringModel): List[Float] = {
    val objects = sm.getWrappedObject
    objects.asInstanceOf[List[Any]].headOption.map {
      case s: StringModel => objects.asInstanceOf[List[StringModel]].flatMap {
        _.asInstanceOf[List[Double]].map(_.toFloat)
      }
      case x: java.lang.Double => objects.asInstanceOf[List[java.lang.Double]].map(_.toFloat)
      case x: Double => objects.asInstanceOf[List[Double]].map(_.toFloat)
      case x: java.lang.Float=> objects.asInstanceOf[List[java.lang.Float]].map(_.toFloat)
      case x: Float => objects.asInstanceOf[List[Float]]
      case s: SimpleScalar => objects.asInstanceOf[List[SimpleScalar]].map(_.getAsString.toFloat)
      case s: SimpleNumber => objects.asInstanceOf[List[SimpleNumber]].map(_.getAsNumber.floatValue())
      case other => throw new Exception(s"Not implemented: list of ${other.getClass} to List[Float].")
    }.getOrElse(Nil)
  }

}
