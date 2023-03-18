import social.xperience.FunctionTrace.Trace

class Test {
    @Trace
    fun test(someString: String) {
        println(someString)
        Thread.sleep(100)
    }
}