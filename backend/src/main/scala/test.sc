
def f(x: Int): Int = ???

def g(x: Float): Int = ???

def w[T](fun: T => Int): String = {
  classOf[T] match
  {
    case Int.getClass =>
  }
}
