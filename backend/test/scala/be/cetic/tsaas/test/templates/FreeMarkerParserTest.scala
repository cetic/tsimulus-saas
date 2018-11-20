package templates

import be.cetic.tsaas.utils.templates.FreeMarkerParser
import org.scalatest.{FlatSpec, Inspectors, Matchers}


class FreeMarkerParserTest extends FlatSpec with Matchers with Inspectors {
  val float = 32.2.toFloat
  val root = Map[String, Any]("float" -> float)


  val tmpl = """This is a float: ${float!0}""".stripMargin

  val p = new FreeMarkerParser(tmpl)

  val trueOutput = "This is a float: 32,2"

  "A FreeMarkerParser" should "parse a template" in {
    p.process(root) shouldBe trueOutput
  }

  "A FreeMarkerParser" should "call wrappers to render" in {
    val tmpl =
      """${b64(x)}""".stripMargin
    val res = new FreeMarkerParser(tmpl).process(Map("x"->12.3.toFloat))
    res shouldBe "zcxEQQ=="
  }

  "A FreeMarkerParser" should "call wrappers to render number lists in big endian" in {
    val tmpl =
      """{"data":"${b64("big", x)}"}""".stripMargin
    val res = new FreeMarkerParser(tmpl).process(Map("x"->List(12.3, 12.2)))
    res shouldBe """{"data":"QUTMzUFDMzM="}"""
  }

  "A FreeMarkerParser" should "call wrappers to render number lists in little endian" in {
    val tmpl =
      """{"data":"${b64("little", x)}"}""".stripMargin
    val res = new FreeMarkerParser(tmpl).process(Map("x"->List(12.3, 12.2)))
    res shouldBe """{"data":"zcxEQTMzQ0E="}"""
  }

  "A FreeMarkerParser" should "add noise to numbers" in {
    val tmpl =
      """${noise(3,x)}""".stripMargin
    val res = new FreeMarkerParser(tmpl).process(Map("x"->12.3))
    res.getClass shouldBe classOf[String]
  }

  "A FreeMarkerParser" should "add noise to words" in {
    val tmpl =
      """${noise("coucou")}""".stripMargin
    val res = new FreeMarkerParser(tmpl).process()
    res.getClass shouldBe classOf[String]
  }

  "A FreeMarkerParser" should "allow combining b64 and noise methods " in {
    val tmpl =
      """${b64(noise(3,x), noise(3,x))}""".stripMargin
    val res = new FreeMarkerParser(tmpl).process(Map("x"->12.3))
    res.getClass shouldBe classOf[String]
  }

  "A FreeMarkerParser" should "allow combining b64, replicate and noise methods" in {
    val tmpl =
      """${b64("little",noise(3, replicate(250,x)))}""".stripMargin
    val res = new FreeMarkerParser(tmpl).process(Map("x"->12.3))
    res.getClass shouldBe classOf[String]
  }

  "A Freemarker parser" should "parse even crappy formats" in {
    val json = """{
  "msg_dt": "${time?number_to_datetime?string["yyyy-MM-dd'T'HH:mm:ss.'000000Z'"]}}",
  "serial_number": "18600006",
  "n_devices": 1,
  "device_1": {
    "metadata": {
      "n": 1,
      "name": "periodic_example",
      "location": "Micromega Dynamics",
      "latitude": 50.701874,
      "longitude": 4.594015,
      "n_channels": 1,
      "type": 1,
      "remote_processing": 1,
      "tlm_type": 1
    },
    "channel_0": {
      "n": 0,
      "name": "example_0",
      "units": "mA",
      "local_processing": 0,
      "sampling_f": 250,
      "n_stats": 0,
      "rms_data": "",
      "avg_data": "",
      "min_data": "",
      "max_data": "",
      "peak_peak_data": "",
      "peak_data": "",
      "skewness_data": "",
      "kurtosis_data": "",
      "std_data": "",
      "n_events": 0,
      "warning": "",
      "alarm": "",
      "n_raw_data": 250,
      "raw_data": "${b64("little", noise(1,replicate(250, x)))}"
    }
  }
}"""
    val res = new FreeMarkerParser(json).process(Map("x"->12.3, "time"->System.currentTimeMillis() ))
    res.getClass shouldBe classOf[String]
  }
}
