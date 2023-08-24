package social.xperience.validator

import social.xperience.Verification
import social.xperience.dto.Email
import social.xperience.dto.Password

class EmailValidator : Verification<Email> {
    private val regex = Regex("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}\$")
    override fun verify(toVerify: Email) {
        if (!regex.matches(toVerify)) {
            throw IllegalArgumentException("Invalid Email")
        }
    }
}

class PasswordValidator : Verification<Password> {
    override fun verify(toVerify: Password) {
        if (toVerify.length > 255) {
            throw IllegalArgumentException("Password length should not exceed 255 characters as we store in plain text(very secure) and this will crash the server")
        }
        if (toVerify.uppercase() != toVerify) {
            throw IllegalArgumentException("Password should only contain uppercase characters")
        }
        if (toVerify.contains(Regex("[0-9]+"))) {
            throw IllegalArgumentException("Password should not contain any numbers")
        }
        if (toVerify.contains(Regex("[^A-Z]+"))) {
            throw IllegalArgumentException("Password should not contain any special character")
        }
    }
}