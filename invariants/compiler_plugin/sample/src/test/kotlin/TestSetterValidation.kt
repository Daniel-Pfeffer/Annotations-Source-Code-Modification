import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import social.xperience.dto.Login
import social.xperience.dto.Register
import social.xperience.entity.UserEntity
import social.xperience.service.MockUserService
import kotlin.math.log

class TestSetterValidation {

    lateinit var mockUserService: MockUserService
    lateinit var user: UserEntity

    val register = Register("MrGewurz", "Daniel", "Pfeffer", "daniel.pfeffer@breathless-pictures.at", "VALID")

    val login = Login("daniel.pfeffer@breathless-pictures.at", "VALID")

    @BeforeEach
    fun setUp() {
        mockUserService = MockUserService()
        mockUserService.register(register)
        user = mockUserService.login(login)
    }

    @Test
    fun testUpdateUser() {
        Assertions.assertThrowsExactly(IllegalArgumentException::class.java) {
            user.profession = "Student"
        }

        Assertions.assertDoesNotThrow {
            user.profession = "Software Developer"
        }

        Assertions.assertDoesNotThrow {
            user.description = "Some description"
        }
    }
}