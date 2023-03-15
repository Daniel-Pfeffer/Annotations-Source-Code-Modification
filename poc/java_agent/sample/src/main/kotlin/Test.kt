package social.xperience.sample

import social.xperience.FunctionTrace.Trace

class Test {
    @Trace
    fun test() {
        println("[Sample] I do dome calculations")
        Thread.sleep(100)
    }
}