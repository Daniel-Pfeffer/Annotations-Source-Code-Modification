import org.jetbrains.kotlin.codegen.ClassBuilder
import org.jetbrains.kotlin.codegen.ClassBuilderFactory
import org.jetbrains.kotlin.codegen.ClassBuilderMode
import org.jetbrains.kotlin.codegen.extensions.ClassBuilderInterceptorExtension
import org.jetbrains.kotlin.diagnostics.DiagnosticSink
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOrigin

class FunctionTraceClassGenerationInterceptor(val annotations: List<String>) : ClassBuilderInterceptorExtension {
    override fun interceptClassBuilderFactory(
        interceptedFactory: ClassBuilderFactory,
        bindingContext: BindingContext,
        diagnostics: DiagnosticSink,
    ): ClassBuilderFactory = object : ClassBuilderFactory by interceptedFactory {
        override fun getClassBuilderMode(): ClassBuilderMode = interceptedFactory.classBuilderMode

        override fun newClassBuilder(origin: JvmDeclarationOrigin): ClassBuilder =
            FunctionTraceClassBuilder(interceptedFactory.newClassBuilder(origin), annotations)


        override fun asText(builder: ClassBuilder?): String =
            interceptedFactory.asText((builder as FunctionTraceClassBuilder).classBuilder)

        override fun asBytes(builder: ClassBuilder?): ByteArray =
            interceptedFactory.asBytes((builder as FunctionTraceClassBuilder).classBuilder)

        override fun close() {
            interceptedFactory.close()
        }
    }
}