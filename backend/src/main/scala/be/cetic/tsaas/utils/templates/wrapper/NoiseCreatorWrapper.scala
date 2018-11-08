package be.cetic.tsaas.utils.templates.wrapper

import java.util

import freemarker.ext.beans.StringModel
import freemarker.template.{SimpleNumber, SimpleScalar, TemplateMethodModelEx}

import scala.collection.JavaConverters._
import scala.util.Random


class NoiseCreatorWrapper[T](val data: T, val sigma: Double = 1) extends BaseWrapper[T, T] {
  val rand = new Random()
  val value: T = computevalue()

  def computevalue() = {
    val res = data match {
      case x: Float => (x + rand.nextGaussian() * sigma).toFloat
      case x: Double => x + rand.nextGaussian() * sigma
      case x: Int => (x + rand.nextGaussian() * sigma).toInt
      case x: Long => (x + rand.nextGaussian() * sigma).toLong
      case b: Boolean => rand.nextBoolean()
      case c: String => rand.nextString(c.size)
      case other => other
    }
    res.asInstanceOf[T]
  }
}

class NoiseCreatorWrapperMethod extends TemplateMethodModelEx {
  def exec(arguments: util.List[_]) = {
    arguments.asScala.toList match {
      case (value: SimpleScalar) :: Nil =>
        val data: String = value.getAsString
        new NoiseCreatorWrapper(data).value.toString

      case (sigma: SimpleNumber) :: (value: SimpleNumber) :: Nil =>
        val data: Double = value.getAsNumber.doubleValue()
        new NoiseCreatorWrapper(data).value.toString

      case (sigma: SimpleNumber) :: (value: StringModel) :: Nil =>
        Parsers.parseStringModel(value).map(new NoiseCreatorWrapper(_).value)

      case other => s"Invalid arguments $other"

    }
  }
}
