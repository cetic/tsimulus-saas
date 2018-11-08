package be.cetic.tsaas.utils.templates.wrapper

import java.nio.ByteOrder
import java.util
import java.util.Base64

import freemarker.ext.beans.StringModel
import freemarker.template.{SimpleNumber, SimpleScalar, TemplateMethodModelEx}

import scala.collection.JavaConverters._

object Base64Wrapper {
  def apply(data: Float, order: ByteOrder): Base64Wrapper = {
    val bytes = java.nio.ByteBuffer.allocate(4)
    bytes.order(order)
    bytes.putFloat(data)
    new Base64Wrapper(bytes.array())
  }

  def apply(data: Traversable[Float], order: ByteOrder): Base64Wrapper = {
    val bytes = java.nio.ByteBuffer.allocate(4 * data.size)
    bytes.order(order)
    var pos = 0
    data.foreach { x =>
      bytes.putFloat(pos, x)
      pos += 4
    }
    new Base64Wrapper(bytes.order(order).array())
  }
}

class Base64Wrapper(val data: Array[Byte]) extends BaseWrapper[Array[Byte], String] {
  val value: String = Base64.getEncoder.encodeToString(data)
}

class Base64WrapperMethod extends TemplateMethodModelEx {
  override def exec(arguments: util.List[_]): String = {

    val args = arguments.asScala.toList
    var datae = List[Float]()
    val orderMap = Map("big" -> ByteOrder.BIG_ENDIAN, "little" -> ByteOrder.LITTLE_ENDIAN)
    val order = args match {
      case (o: SimpleScalar) :: (rest: List[Any]) if orderMap.keys.toSet.contains(o.getAsString) =>
        datae = parseList(rest)
        orderMap(o.toString)

      case l: List[Any] =>
        datae = parseList(l)
        ByteOrder.nativeOrder()

      case _ => throw new Exception("Unknown format")
    }
    Base64Wrapper(datae, order).value
  }

  private def parseList(list: List[Any]): List[Float] = {
    list.headOption.map {
      case s: StringModel => Parsers.parseStringModel(s)
      case s: SimpleScalar => list.asInstanceOf[List[SimpleScalar]].map(_.getAsString.toFloat)
      case s: SimpleNumber => list.asInstanceOf[List[SimpleNumber]].map(_.getAsNumber.floatValue)
      case other => throw new Exception(s"Not implemented: list of ${other.getClass} to List[Float].")
    }.getOrElse(Nil)
  }


}

