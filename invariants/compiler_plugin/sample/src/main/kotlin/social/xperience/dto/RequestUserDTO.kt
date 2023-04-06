package social.xperience.social.xperience.dto

import social.xperience.Holds
import social.xperience.social.xperience.validator.ProfessionValidator
import social.xperience.social.xperience.validator.UpdateValidator

@Holds(UpdateValidator::class)
data class Update(
    val firstname: String? = null,
    val lastname: String? = null,
    val username: String? = null,
    @Holds(ProfessionValidator::class)
    val profession: String? = null,
    val description: String? = null,
    val birthday: Long? = null,
    val languages: Set<String>? = null,
)

data class Login(val email: String, val password: String)

data class Register(
    val username: String? = null,
    val firstname: String,
    val lastname: String,
    val email: String,
    val password: String,
    val options: RegisterOptions = RegisterOptions(),
) {
    data class RegisterOptions(
        val skipPicture: Boolean = false,
        val skipAdminChat: Boolean = false,
        val skipAutomaticFriendRequests: Boolean = false,
    )
}

data class Filter(
    val postalCode: String? = null,
    val locationRadius: Double?,
    val minAge: Int?,
    val maxAge: Int?,
)