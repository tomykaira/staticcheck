package example

case class Test(
                 error: String = "",
                 value1: Int = 0,
                 value2: Int = 0,
               )

object Main {
  def main(args: Array[String]): Unit = {
    println(Test(error = "error dayo"))
    println(Test("error dayo"))
    println(Test(value1 = 1, value2 = 2))
    println(Test(value1 = 1)) // should be error
    println(Test(error = "error dayo", value1 = 1, value2 = 2)) // should be error

    println(Test2(error = "error dayo"))
    println(Test2("error dayo"))
    println(Test2(value1 = 1, value2 = 2))
    println(Test2(value1 = 1)) // should be error
    println(Test2(error = "error dayo", value1 = 1, value2 = 2)) // should be error
  }
}
