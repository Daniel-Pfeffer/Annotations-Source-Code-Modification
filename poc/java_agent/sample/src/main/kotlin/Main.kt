package social.xperience.sample

import social.xperience.FunctionTrace.Trace

@Trace
fun main() {
    val x = 23
    val z = 23
    hello("World", x)
    val y = 32
    test(y)
    Thread.sleep(500)
    test(z)
}


@Trace
fun hello(type: String, y: Int) {
    println("Hello $type")
    Thread.sleep(1000)
    test(y)
}


@Trace
fun test(x: Int) {
    println(x)
}