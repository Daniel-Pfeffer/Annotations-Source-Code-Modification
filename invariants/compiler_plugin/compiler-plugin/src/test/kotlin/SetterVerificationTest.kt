@file:OptIn(ExperimentalCompilerApi::class)

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import social.xperience.cli.InvariantRegistrar
import java.lang.reflect.InvocationTargetException

class SetterVerificationTest {

    @Test
    fun validTest() {
        Assertions.assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
        val clazz = result.classLoader.loadClass("social.xperience.TestClassKt")
        clazz.declaredMethods.first { it.name == "mainValid" }.invoke(null)
    }

    @Test
    fun invalidAgeTest() {
        Assertions.assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode) { result.messages }
        val clazz = result.classLoader.loadClass("social.xperience.TestClassKt")
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            callUnwrapped(clazz, "mainInvalidAge")
        }.also {
            Assertions.assertEquals("Must be at least 18 years old to register", it.message)
        }
    }

    @Test
    fun invalidAgeTest2() {
        Assertions.assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode) { result.messages }
        val clazz = result.classLoader.loadClass("social.xperience.TestClassKt")
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            callUnwrapped(clazz, "mainInvalidAge2")
        }.also {
            Assertions.assertEquals("Must not be 60 years old to register", it.message)
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
                data class TestUserData( @Holds(MustBe18::class) val age: Age, val username: String)
                
                class UserEntity (
                    val username: String,
                    @Holds(MustBeLess60::class)
                    @Holds(MustBe18::class)
                    var age: Age
                )

                class Person{
                    val name: String = "" 
                    constructor() {}
                }
                
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

                class TestVerification2 : Verification<TestUserData> {
                    override fun verify(toVerify: TestUserData) {
                        if (toVerify.username.length < 5) {
                            throw IllegalArgumentException("Username must be at least 5 chars long")
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

                class MustBeLess60 : Verification<Age> {
                    override fun verify(toVerify: Age) {
                        if (toVerify.value >= 60) {
                            throw IllegalArgumentException("Must not be 60 years old to register")
                        }
                    }
                }
            """
            )

            val main = SourceFile.kotlin(
                "TestClass.kt", """
            package social.xperience

            fun mainValid(){
                val user = UserEntity("MrAdult", Age(22))
                user.age = Age(21)
                println(user)
            }

            fun mainInvalidAge(){
                // this should throw as user is too young
                val user = UserEntity("MrAdult", Age(22))
                user.age = Age(17)
                println(user)
            }

            fun mainInvalidAge2(){
                // this should throw as user is too young
                val user = UserEntity("MrAdult", Age(22))
                user.age = Age(100)
                println(user)
            }
        """
            )

            result = KotlinCompilation().apply {
                sources = listOf(dataClasses, verifier, main)
                kotlincArguments = listOf("-Xcontext-receivers")
                compilerPluginRegistrars = listOf(InvariantRegistrar())
                useIR = true
                useK2 = true
                inheritClassPath = true
            }.compile()
        }
    }
}