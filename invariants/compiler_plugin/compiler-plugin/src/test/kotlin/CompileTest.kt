import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import social.xperience.cli.InvariantCommandlineProcessor
import social.xperience.cli.InvariantRegistrar

class CompileTest {
    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun testSomething() {
        val kotlinSource = SourceFile.kotlin(
            "TestClass.kt", """
            package social.xperience

            @Target(
                AnnotationTarget.FUNCTION,
                AnnotationTarget.TYPE_PARAMETER,
                AnnotationTarget.FIELD,
                AnnotationTarget.LOCAL_VARIABLE,
                AnnotationTarget.CLASS,
                AnnotationTarget.CONSTRUCTOR,
                AnnotationTarget.PROPERTY
            )
            @Retention(AnnotationRetention.SOURCE)
            annotation class Holds()

            class Test {
                @Holds
                private val x: String = "Test"
            
                @Holds
                private val y: Long = 23
            }
        """
        )

        val result = KotlinCompilation().apply {
            sources = listOf(kotlinSource)
            compilerPluginRegistrars = listOf(InvariantRegistrar())
            commandLineProcessors = listOf(InvariantCommandlineProcessor())
            messageOutputStream = System.out
        }.compile()

        Assertions.assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }
}