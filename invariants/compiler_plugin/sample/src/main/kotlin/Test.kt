package social.xperience


class Test {
    private val x: String = "Test"

    private val y: Long = 23

    private var z: Long = 23
    fun testXy() {
        z + 2
        Pool.longverification.verify(z)
    }

    fun doNothing() {
        println("I do nothing at all")
    }
}


class LongVerification : Verification<Long> {
    override fun verify(toVerify: Long) {
        TODO("Not yet implemented")
    }
}

class TestVerification : Verification<String> {
    override fun verify(toVerify: String) {
        TODO("Not yet implemented")
    }
}

object Pool{
    val longverification = LongVerification()
}