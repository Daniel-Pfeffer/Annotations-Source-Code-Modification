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

            import social.xperience.common.FunctionVerifierClass
            
            
            data class Test(val x: Int, val y: Int) {
                init{
                    Pool.testverification.verify(this)
                }
            }
            
            class TestVerification : Verification<Test> {
                override fun verify(toVerify: Test) {
                    if (toVerify.x < 0) {
                        throw IllegalStateException("")
                    }
                }
            }
            
            class StringFunctionVerification : Verification<FunctionVerifierClass.FunctionVerifier1<String>> {
                override fun verify(toVerify: FunctionVerifierClass.FunctionVerifier1<String>) {
                    val string = toVerify.a
                    if (string.length < 4) {
                        throw IllegalStateException("Length more than 4 required")
                    }
                }
            }
            
            object Pool {
                val testverification = TestVerification()
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