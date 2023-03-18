import social.xperience.Holds
import social.xperience.Verification

class Test {
    @Holds(TestVerification::class)
    private val x: String = "Test"

    @Holds(LongVerification::class)
    private val y: Long = 23
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