package social.xperience.entity

import social.xperience.Holds
import social.xperience.dto.Update
import social.xperience.validator.EmailValidator
import social.xperience.validator.PasswordValidator
import social.xperience.validator.ProfessionValidator
import java.time.LocalDateTime
import java.time.ZoneOffset

class UserEntity(
    var username: String,
    var firstname: String,
    var lastname: String,
    @Holds(EmailValidator::class)
    val email: String,
    @Holds(PasswordValidator::class)
    val password: String,
    @Holds(ProfessionValidator::class)
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