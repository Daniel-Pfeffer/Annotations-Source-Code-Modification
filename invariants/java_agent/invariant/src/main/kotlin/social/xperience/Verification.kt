package social.xperience

import kotlin.jvm.Throws

interface Verification<T> {
    @Throws(VerificationException::class)
    fun verify(toVerify: T)
}