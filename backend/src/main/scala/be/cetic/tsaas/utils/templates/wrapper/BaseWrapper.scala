package be.cetic.tsaas.utils.templates.wrapper

trait BaseWrapper[T, U] {
  val data : T

  val value: U
}
