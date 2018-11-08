package be.cetic.tsaas.test.templates

import java.nio.ByteOrder

import be.cetic.tsaas.utils.templates.wrapper.{Base64Wrapper, NoiseCreatorWrapper}
import org.scalatest.{FlatSpec, Inspectors, Matchers}

class WrapperTest extends FlatSpec with Matchers with Inspectors {
  "A base64 wrapper" should "encode a floating number" in {
    val b64 = Base64Wrapper(12.2.toFloat, order = ByteOrder.LITTLE_ENDIAN).value
    b64 shouldBe "MzNDQQ=="
  }


  "A base64 wrapper" should "encode a list of floating numbers" in {
    val b64 = Base64Wrapper(List(12.2.toFloat, 12.3.toFloat), order = ByteOrder.LITTLE_ENDIAN).value
    b64 shouldBe "MzNDQc3MREE="
  }

  "A noise creator wrapper" should "add noise to int numbers" in {
    val noise = new NoiseCreatorWrapper(13, 4)
    noise.value.getClass shouldBe classOf[Int]
  }

  "A noise creator wrapper" should "add noise to float numbers" in {
    val noise = new NoiseCreatorWrapper(13.3.toFloat, 4)
    noise.value.getClass shouldBe classOf[Float]
  }


  "A noise creator wrapper" should "add noise to bools" in {
    val noise = new NoiseCreatorWrapper(true)
    noise.value.getClass shouldBe classOf[Boolean]
  }

}


