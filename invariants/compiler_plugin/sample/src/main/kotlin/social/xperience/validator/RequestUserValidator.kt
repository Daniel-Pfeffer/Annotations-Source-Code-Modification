package social.xperience.social.xperience.validator

import social.xperience.Verification
import social.xperience.social.xperience.dto.RequestUserDTO
import java.lang.IllegalArgumentException

class UpdateValidator : Verification<RequestUserDTO.Update> {
    override fun verify(toVerify: RequestUserDTO.Update) {
        with(toVerify) {
            if (firstname == null && lastname == null && username == null && profession == null && description == null && birthday == null && languages == null) {
                throw IllegalStateException("Updating nothing is not supported!")
            }
            if (firstname != null) {
                if (firstname.codePoints().count() > 100) {
                    throw IllegalArgumentException("Firstname max length is 100")
                }
                if (firstname.codePoints().count() < 2) {
                    throw IllegalArgumentException("Fistname min length is 2")
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

    private val professions = listOf("Software Developer", "Software Architect", "Lecturer")

    private fun isValidProfession(profession: String): Boolean {
        return profession in professions
    }
}