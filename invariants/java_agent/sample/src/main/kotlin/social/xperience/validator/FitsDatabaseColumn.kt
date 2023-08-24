package social.xperience.validator

import social.xperience.Verification

class FitsDatabaseColumn : Verification<String?> {
    override fun verify(toVerify: String?) {
        if (toVerify != null && toVerify.length > 255) {
            throw IllegalArgumentException("\"$toVerify\" length must be less than 256 characters")
        }
    }
}