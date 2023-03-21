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
            annotation class Holds

            class Test {
                @field:Holds
                private val x: String = "Test"
            
                @field:Holds
                private val y: Long = 23

                // on visit property: z has an annotation as the annotation is not "targeted"
                // kotlin will generate
                @Holds
                private var z: Long = 24

                fun doSomething(){
                    z+=y+x.length
                }

                override fun toString(): String{
                    return "$"+"x, "+"$"+"y, " + "$"+"z"
                }
            }

            fun callTest(){
                println(Test().toString())
            }
        """
        )

        val result = KotlinCompilation().apply {
            sources = listOf(kotlinSource)
            compilerPluginRegistrars = listOf(InvariantRegistrar())
            useIR = true
            inheritClassPath = true
        }.compile()

        Assertions.assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }
}