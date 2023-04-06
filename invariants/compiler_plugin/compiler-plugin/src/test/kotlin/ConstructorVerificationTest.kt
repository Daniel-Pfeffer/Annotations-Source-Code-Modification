@file:OptIn(ExperimentalCompilerApi::class)

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import social.xperience.cli.InvariantRegistrar
import java.lang.reflect.InvocationTargetException


class ConstructorVerificationTest {
    @Test
    fun validTest() {
        Assertions.assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
        val clazz = result.classLoader.loadClass("social.xperience.TestClassKt")
        clazz.declaredMethods.first { it.name == "mainValid" }.invoke(null)
    }

    @Test
    fun invalidAgeTest() {
        Assertions.assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
        val clazz = result.classLoader.loadClass("social.xperience.TestClassKt")
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            callUnwrapped(clazz, "mainInvalidAge")
        }.also {
            Assertions.assertEquals("Must be at least 18 years old to register", it.message)
        }
    }

    @Test
    fun invalidUsernameTest() {
        Assertions.assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
        val clazz = result.classLoader.loadClass("social.xperience.TestClassKt")
        Assertions.assertThrowsExactly(IllegalArgumentException::class.java) {
            callUnwrapped(clazz, "mainInvalidUsername")
        }.also {
            Assertions.assertEquals("Username must be at least 4 chars long", it.message)
        }
    }

    @Test
    fun invalidAgeAndUsernameTest() {
        Assertions.assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
        val clazz = result.classLoader.loadClass("social.xperience.TestClassKt")
        // More generic verification is done before the more specific, so class Holds are executed before property Holds
        Assertions.assertThrowsExactly(IllegalArgumentException::class.java) {
            callUnwrapped(clazz, "mainInvalidBoth")
        }.also {
            Assertions.assertEquals("Username must be at least 4 chars long", it.message)
        }
    }

    private fun callUnwrapped(clazz: Class<*>, methodName: String) {
        try {
            clazz.declaredMethods.first { it.name == methodName }.invoke(null)
        } catch (e: InvocationTargetException) {
            throw e.targetException
        }
    }

    companion object {

        lateinit var result: KotlinCompilation.Result

        @JvmStatic
        @BeforeAll
        fun setUp() {
            val dataClasses = SourceFile.kotlin(
                "DataClasses.kt", """
                package social.xperience

                @Holds(TestVerification::class) 
                data class TestUserData(@Holds(MustBe18::class) val age: Age, val username: String)
                
                
                @JvmInline
                value class Age(val value: Int)
            """
            )

            val verifier = SourceFile.kotlin(
                "Verifier.kt", """
                package social.xperience

                class TestVerification : Verification<TestUserData> {
                    override fun verify(toVerify: TestUserData) {
                        if (toVerify.username.length < 4) {
                            throw IllegalArgumentException("Username must be at least 4 chars long")
                        }
                    }
                }
                
                class MustBe18 : Verification<Age> {
                    override fun verify(toVerify: Age) {
                        if (toVerify.value < 18) {
                            throw IllegalArgumentException("Must be at least 18 years old to register")
                        }
                    }
                }
            """
            )

            val main = SourceFile.kotlin(
                "TestClass.kt", """
            package social.xperience

            fun mainValid(){
                val user = TestUserData(Age(22), "MrAdult")
                println(user)
            }

            fun mainInvalidAge(){
                // this should throw as user is too young
                val user = TestUserData(Age(17), "MrTeenager")
                println(user)
            }

            fun mainInvalidUsername(){
                // this should throw as user's username is too short
                val user = TestUserData(Age(18), "Mr")
                println(user)
            }

            fun mainInvalidBoth(){
                // this should throw as user is too young
                val user = TestUserData(Age(17), "Mr")
                println(user)
            }
        """
            )

            result = KotlinCompilation().apply {
                sources = listOf(dataClasses, verifier, main)
                kotlincArguments = listOf("-Xcontext-receivers")
                compilerPluginRegistrars = listOf(InvariantRegistrar())
                useIR = true
                inheritClassPath = true
            }.compile()
        }
    }
}