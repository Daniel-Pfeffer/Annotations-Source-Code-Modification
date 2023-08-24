import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import social.xperience.dto.Register
import social.xperience.dto.Update
import kotlin.IllegalArgumentException

open class TestConstructorValidation {
    @Test
    fun testValidUpdateDTO() {
        Assertions.assertDoesNotThrow {
            Update("Daniel", "Pfeffer", "MrGewurz", "Software Developer")
        }
    }

    @Test
    fun testInvalidUpdateDTO() {
        Assertions.assertThrowsExactly(IllegalStateException::class.java) {
            Update()
        }.also {
            Assertions.assertEquals("Updating nothing is not supported!", it.message)
        }

        Assertions.assertThrowsExactly(IllegalArgumentException::class.java) {
            Update("D", "Pfeffer")
        }.also {
            Assertions.assertEquals("Firstname min length is 2", it.message)
        }

        Assertions.assertThrowsExactly(IllegalArgumentException::class.java) {
            Update(String.random(101), "Pfeffer")
        }.also {
            Assertions.assertEquals("Firstname max length is 100", it.message)
        }

        Assertions.assertThrowsExactly(IllegalArgumentException::class.java) {
            Update("Daniel", "P")
        }.also {
            Assertions.assertEquals("Lastname min length is 2", it.message)
        }

        Assertions.assertThrowsExactly(IllegalArgumentException::class.java) {
            Update("Daniel", String.random(101))
        }.also {
            Assertions.assertEquals("Lastname max length is 100", it.message)
        }

        Assertions.assertThrowsExactly(IllegalArgumentException::class.java) {
            Update("Daniel", "Pfeffer", profession = String.random(2049))
        }.also {
            Assertions.assertEquals("Profession max length is 2048", it.message)
        }

        Assertions.assertThrowsExactly(IllegalArgumentException::class.java) {
            Update("Daniel", "Pfeffer", profession = "Student")
        }.also {
            Assertions.assertEquals("Profession is not valid", it.message)
        }
    }

    @Test
    fun testValidRegisterDTO() {
        Register("MrGewurz", "Daniel", "Pfeffer", "daniel.pfeffer@breathless-pictures.at", "THISISAPASSWORD")
    }

    @Test
    fun testInvalidRegisterDTO() {
        Assertions.assertThrowsExactly(IllegalArgumentException::class.java) {
            Register("MrGewurz", "D", "Pfeffer", "daniel.pfeffer@breathless-pictures.at", "THISISAPASSWORD")
        }.also {
            Assertions.assertEquals("Firstname min length is 2", it.message)
        }

        Assertions.assertThrowsExactly(IllegalArgumentException::class.java) {
            Register(
                "MrGewurz",
                String.random(101),
                "Pfeffer",
                "daniel.pfeffer@breathless-pictures.at",
                "THISISAPASSWORD"
            )
        }.also {
            Assertions.assertEquals("Firstname max length is 100", it.message)
        }

        Assertions.assertThrowsExactly(IllegalArgumentException::class.java) {
            Register("MrGewurz", "Daniel", "P", "daniel.pfeffer@breathless-pictures.at", "THISISAPASSWORD")
        }.also {
            Assertions.assertEquals("Lastname min length is 2", it.message)
        }

        Assertions.assertThrowsExactly(IllegalArgumentException::class.java) {
            Register(
                "MrGewurz",
                "Daniel",
                String.random(101),
                "daniel.pfeffer@breathless-pictures.at",
                "THISISAPASSWORD"
            )

        }.also {
            Assertions.assertEquals("Lastname max length is 100", it.message)
        }

        Assertions.assertThrowsExactly(IllegalArgumentException::class.java) {
            Register(
                "MrGewurz",
                "Daniel",
                "Pfeffer",
                "invalidmail",
                "THISISAPASSWORD"
            )
        }.also {
            Assertions.assertEquals("Invalid Email", it.message)
        }

        Assertions.assertThrowsExactly(IllegalArgumentException::class.java) {
            Register(
                "MrGewurz",
                "Daniel",
                "Pfeffer",
                "daniel.pfeffer@breathless-pictures.at",
                String.random(256)
            )
        }.also {
            Assertions.assertEquals(
                "Password length should not exceed 255 characters as we store in plain text(very secure) and this will crash the server",
                it.message
            )
        }

        Assertions.assertThrowsExactly(IllegalArgumentException::class.java) {
            Register(
                "MrGewurz",
                "Daniel",
                "Pfeffer",
                "daniel.pfeffer@breathless-pictures.at",
                "thisisinvalid"
            )
        }.also {
            Assertions.assertEquals(
                "Password should only contain uppercase characters",
                it.message
            )
        }

        Assertions.assertThrowsExactly(IllegalArgumentException::class.java) {
            Register(
                "MrGewurz",
                "Daniel",
                "Pfeffer",
                "daniel.pfeffer@breathless-pictures.at",
                "TH1S1SALS01NVAL1D"
            )
        }.also {
            Assertions.assertEquals(
                "Password should not contain any numbers",
                it.message
            )
        }

        Assertions.assertThrowsExactly(IllegalArgumentException::class.java) {
            Register(
                "MrGewurz",
                "Daniel",
                "Pfeffer",
                "daniel.pfeffer@breathless-pictures.at",
                "THISISALSOALSOINVALID!"
            )
        }.also {
            Assertions.assertEquals(
                "Password should not contain any special character",
                it.message
            )
        }
    }


    private fun String.Companion.random(length: Int): String {
        var str = ""
        val charPool = ('a'..'z') + ('A'..'Z')
        repeat(length) {
            str += charPool.random()
        }
        return str
    }
}