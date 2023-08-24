package social.xperience.validator

import social.xperience.Verification
import social.xperience.common.FunctionVerifierClass
import social.xperience.dto.Update

class DoSomethingVerifier : Verification<FunctionVerifierClass.FunctionVerifier1<Update>> {
    override fun verify(toVerify: FunctionVerifierClass.FunctionVerifier1<Update>) {
        val update = toVerify.a
        with(update) {
            if (firstname.isNullOrBlank()) {
                throw IllegalArgumentException("Firstname has to be non blank string")
            }
        }
    }
}