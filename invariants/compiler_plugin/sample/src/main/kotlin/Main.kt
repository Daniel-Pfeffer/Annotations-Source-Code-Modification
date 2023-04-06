package social.xperience

import social.xperience.social.xperience.dto.RequestUserDTO

fun main() {
    val emptyUpdate = RequestUserDTO.Update()
    doSomething(emptyUpdate)
}


fun doSomething(with: RequestUserDTO.Update) {
    println(with)
}

@Holds(TestVerification::class)
data class TestUserData(@Holds(MustBe18::class) val age: Age, val username: String)


@JvmInline
value class Age(val value: Int)

class TestVerification : Verification<TestUserData> {
    override fun verify(toVerify: TestUserData) {
        if (toVerify.username.length < 4) {
            throw IllegalArgumentException("Username must be at least 4 chars long")
        }
    }
}

class MustBe18 : Verification<Age> {
    override fun verify(toVerify: Age) {
        if (toVerify.value < 18) {
            throw IllegalArgumentException("Must be at least 18 years old to register")
        }
    }
}


data class TestX(val x: Int, val y: Int) {
    init{
        Pool.testverification.verify(this)
    }
}

class TestXVerification : Verification<TestX> {
    override fun verify(toVerify: TestX) {
        if (toVerify.x < 0) {
            throw IllegalStateException("")
        }
    }
}

object Pool {
    val testverification = TestXVerification()
}