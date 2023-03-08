import com.google.auto.service.AutoService
import org.jetbrains.kotlin.codegen.extensions.ClassBuilderInterceptorExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration

@OptIn(ExperimentalCompilerApi::class)
@AutoService(CompilerPluginRegistrar::class)
class FunctionTraceRegistrar : CompilerPluginRegistrar() {
    override val supportsK2: Boolean = false

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        if (!configuration.get(KeyOptions.KEY_ENABLED, true)) {
            return
        }
        val annotations = configuration.get(KeyOptions.KEY_ANNOTATIONS, listOf("social.xperience.FunctionTrace.Trace"))

        ClassBuilderInterceptorExtension.registerExtension(FunctionTraceClassGenerationInterceptor(annotations))
    }
}