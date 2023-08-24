package social.xperience.service

import social.xperience.Holds
import social.xperience.dto.Login
import social.xperience.dto.Register
import social.xperience.dto.Update
import social.xperience.entity.UserEntity
import social.xperience.validator.LoggedInValidator
import social.xperience.validator.SimpleLoggedInValidator

open class MockUserService {

    private val users = mutableListOf<UserEntity>()

    private val charPool = ('a'..'z') + ('A'..'Z') + ('0'..'9')

    fun register(register: Register) {
        val existsUserWithMailOrUsername = users.any { it.email == register.email || it.username == register.username }

        if (existsUserWithMailOrUsername) {
            throw IllegalStateException("User with the given email or username already exists")
        }
        users.add(
            with(register) {
                UserEntity(
                    username ?: List(20) { charPool.random() }.joinToString(""),
                    firstname,
                    lastname,
                    email,
                    password
                )
            }
        )
    }

    fun login(login: Login): UserEntity {
        val user = users.firstOrNull { it.email == login.email }
            ?: throw IllegalArgumentException("User with email: ${login.email} could not be found")

        if (user.password != login.password) {
            throw IllegalArgumentException("User with email ${login.email} could not be found")
        }
        user.loggedIn = true
        return user
    }

    @Holds(LoggedInValidator::class)
    fun logout(user: UserEntity) {
        user.loggedIn = false
    }

    fun update(@Holds(SimpleLoggedInValidator::class) user: UserEntity, update: Update): UserEntity {
        user.update(update)
        return user
    }

    fun updateProfession(@Holds(SimpleLoggedInValidator::class) user: UserEntity, profession: String) {
        user.profession = profession
    }
}