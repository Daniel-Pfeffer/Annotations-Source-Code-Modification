package social.xperience

import social.xperience.common.FunctionVerifierClass


class Test {
    private val x: String = "Test"

    private val y: Long = 23

    private var z: Long = 23
    fun testXy() {
        z + 2
    }

    @Holds(StringFunctionVerification::class)
    fun doNothing(hello: String) {
        println("I do nothing at all $hello")
    }
}


class LongVerification : Verification<Long> {
    override fun verify(toVerify: Long) {
        TODO("Not yet implemented")
    }
}

class StringFunctionVerification : Verification<FunctionVerifierClass.FunctionVerifier1<String>> {
    override fun verify(toVerify: FunctionVerifierClass.FunctionVerifier1<String>) {
        val string = toVerify.a
        if (string.length < 4) {
            throw IllegalStateException("Length more than 4 required")
        }
    }
}

class TestVerification : Verification<String> {
    override fun verify(toVerify: String) {
        TODO("Not yet implemented")
    }
}