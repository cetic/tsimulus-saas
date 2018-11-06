import java.util.Base64

import be.cetic.tsaas.utils.templates.FreeMarkerParser


val float : Seq[Float]= Seq(11.34.toFloat, 11.3.toFloat, 11.23.toFloat, 11.42.toFloat)

val bb = java.nio.ByteBuffer.allocate(4*float.size)
var pos=0
float.foreach{x=>
  bb.putFloat(pos, x)
  pos+=4
}
val ba= bb.array()

Base64.getEncoder().encodeToString(ba)
val root = Map("float"->float)

val tmpl= """
  |This is a float array: ${float}
""".stripMargin

val p= new FreeMarkerParser(tmpl)

p.process(root)