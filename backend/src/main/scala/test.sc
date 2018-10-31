
val s= Stream.range(1,10)
s.filterNot(_ < 5)
s.toIterator.next()

import scala.concurrent.duration._

val t= -1 seconds
t