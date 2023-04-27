package social.xperience.dto

import social.xperience.Holds
import social.xperience.validator.*

@Holds(UpdateValidator::class)
data class Update(
    val firstname: String? = null,
    val lastname: String? = null,
    val username: String? = null,
    @Holds(ProfessionValidator::class)
    val profession: String? = null,
    val description: String? = null,
    val birthday: Long? = null,
)

data class Login(
    @Holds(EmailValidator::class)
    val email: Email,
    @Holds(PasswordValidator::class)
    val password: Password,
)

typealias Email = String

typealias Password = String

@Holds(RegisterValidator::class)
data class Register(
    val username: String? = null,
    val firstname: String,
    val lastname: String,
    @Holds(EmailValidator::class)
    val email: Email,
    @Holds(PasswordValidator::class)
    val password: Password,
)