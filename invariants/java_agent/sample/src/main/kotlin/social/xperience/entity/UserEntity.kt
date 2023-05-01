package social.xperience.entity

import social.xperience.dto.Update
import java.time.LocalDateTime
import java.time.ZoneOffset

class UserEntity(
    var username: String,
    var firstname: String,
    var lastname: String,
    val email: String,
    val password: String,
    var profession: String? = null,
    var description: String? = null,
    var birthday: LocalDateTime? = null,
) {
    var loggedIn: Boolean = false

    fun update(dto: Update) {
        username = dto.username ?: username
        firstname = dto.firstname ?: firstname
        lastname = dto.lastname ?: lastname
        profession = dto.profession ?: profession
        description = dto.description ?: description
        if (dto.birthday != null) {
            birthday = LocalDateTime.ofEpochSecond(dto.birthday, 0, ZoneOffset.UTC)
        }
    }
}