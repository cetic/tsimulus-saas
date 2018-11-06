package be.cetic.tsaas.utils.templates.wrapper

import java.util

import freemarker.template.{SimpleNumber, SimpleScalar, TemplateMethodModelEx}

import scala.collection.JavaConverters._

class ReplicatorWrapper[T](val data: T, count: Int) extends BaseWrapper[T, List[T]] {
  val value: List[T] = List.range(0, count).map(_ => data)
}

class ReplicatorWrapperMethod extends TemplateMethodModelEx {
  def exec(arguments: util.List[_]): AnyRef = {
    val args= arguments.asScala.toList
    args match{
      case (n: SimpleNumber)::rest :: Nil =>List.range(0, n.getAsNumber.intValue).map(_=>rest)
      case _ => throw new Exception("Couldn't apply replicator.")
    }
  }
}