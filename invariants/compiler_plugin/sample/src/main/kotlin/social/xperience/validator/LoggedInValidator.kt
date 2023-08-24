package social.xperience.validator

import social.xperience.Verification
import social.xperience.common.FunctionVerifierClass
import social.xperience.entity.UserEntity

class LoggedInValidator : Verification<FunctionVerifierClass.FunctionVerifier1<UserEntity>> {
    override fun verify(toVerify: FunctionVerifierClass.FunctionVerifier1<UserEntity>) {
        val user = toVerify.a
        if (!user.loggedIn) {
            throw IllegalArgumentException("User has to be logged in")
        }
    }
}

class SimpleLoggedInValidator : Verification<UserEntity> {
    override fun verify(toVerify: UserEntity) {
        if (!toVerify.loggedIn) {
            throw IllegalArgumentException("User has to be logged in")
        }
    }
}