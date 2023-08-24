package social.xperience.validator

import social.xperience.Verification
import social.xperience.dto.Login
import social.xperience.dto.Register
import social.xperience.dto.Update
import java.lang.IllegalArgumentException

class UpdateValidator : Verification<Update> {
    override fun verify(toVerify: Update) {
        with(toVerify) {
            if (firstname == null && lastname == null && username == null && profession == null && description == null && birthday == null) {
                throw IllegalStateException("Updating nothing is not supported!")
            }
            if (firstname != null) {
                if (firstname.codePoints().count() > 100) {
                    throw IllegalArgumentException("Firstname max length is 100")
                }
                if (firstname.codePoints().count() < 2) {
                    throw IllegalArgumentException("Firstname min length is 2")
                }
            }
            if (lastname != null) {
                if (lastname.codePoints().count() > 100) {
                    throw IllegalArgumentException("Lastname max length is 100")
                }
                if (lastname.codePoints().count() < 2) {
                    throw IllegalArgumentException("Lastname min length is 2")
                }
            }
        }
    }
}

class ProfessionValidator : Verification<String?> {
    private val professions = listOf("Software Developer", "Software Architect", "Lecturer")

    override fun verify(toVerify: String?) {
        if (toVerify != null) {
            if (toVerify.codePoints().count() > 2048) {
                throw IllegalArgumentException("Profession max length is 2048")
            }
            if (!isValidProfession(toVerify)) {
                throw IllegalArgumentException("Profession is not valid")
            }
        }
    }

    private fun isValidProfession(profession: String): Boolean {
        return profession in professions
    }
}

class RegisterValidator : Verification<Register> {
    override fun verify(toVerify: Register) {
        with(toVerify) {
            if (firstname.codePoints().count() > 100) {
                throw IllegalArgumentException("Firstname max length is 100")
            }
            if (firstname.codePoints().count() < 2) {
                throw IllegalArgumentException("Firstname min length is 2")
            }

            if (lastname.codePoints().count() > 100) {
                throw IllegalArgumentException("Lastname max length is 100")
            }
            if (lastname.codePoints().count() < 2) {
                throw IllegalArgumentException("Lastname min length is 2")
            }
        }
    }
}