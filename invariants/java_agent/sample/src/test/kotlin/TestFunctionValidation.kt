import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import social.xperience.dto.Login
import social.xperience.dto.Register
import social.xperience.dto.Update
import social.xperience.entity.UserEntity
import social.xperience.service.MockUserService

class TestFunctionValidation {

    lateinit var mockUserService: MockUserService

    lateinit var user: UserEntity

    val register = Register("MrGewurz", "Daniel", "Pfeffer", "daniel.pfeffer@breathless-pictures.at", "VALID")

    val login = Login("daniel.pfeffer@breathless-pictures.at", "VALID")

    @BeforeEach
    fun setup() {
        mockUserService = MockUserService()
        mockUserService.register(register)
        user = mockUserService.login(login)
    }

    @Test
    fun testLoggedInValidator() {
        // we assume the user is logged in
        mockUserService.logout(user)

        Assertions.assertThrowsExactly(IllegalArgumentException::class.java) {
            mockUserService.logout(user)
        }

        Assertions.assertThrowsExactly(IllegalArgumentException::class.java) {
            mockUserService.update(user, Update("Daniel"))
        }

        Assertions.assertDoesNotThrow {
            mockUserService.login(login)
        }

        Assertions.assertDoesNotThrow {
            mockUserService.update(user, Update("Daniel"))
        }

        Assertions.assertDoesNotThrow {
            mockUserService.logout(user)
        }
    }
}