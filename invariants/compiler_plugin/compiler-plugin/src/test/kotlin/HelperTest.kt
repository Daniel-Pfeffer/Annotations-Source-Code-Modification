import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import social.xperience.cli.InvariantRegistrar

class HelperTest {
    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun fullTest() {
        val kotlinSource = SourceFile.kotlin(
            "TestClass.kt", """
            package social.xperience

            class Test {
                private val x: String = "Test"
            
                private val y: Long = 23
            
                private var z: Long = 23
                fun testXy() {
                    z + 2
                }
            
                fun doNothing() {
                    println("I do nothing at all")
                }
            }
            
            
            class LongVerification : Verification<Long> {
                override fun verify(toVerify: Long) {
                    TODO("Not yet implemented")
                }
            }
        """
        )

        val result = KotlinCompilation().apply {
            sources = listOf(kotlinSource)
            compilerPluginRegistrars = listOf(InvariantRegistrar())
            useIR = true
            inheritClassPath = true
        }.compile()
        val clazz = result.classLoader.loadClass("social.xperience.Test")
        val invoked = clazz.constructors.first().newInstance()
        clazz.declaredMethods.single { it.name == "testXy" }.invoke(invoked)
        result.generatedFiles.forEach {
            println(it.absolutePath)
        }

        Assertions.assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }
}