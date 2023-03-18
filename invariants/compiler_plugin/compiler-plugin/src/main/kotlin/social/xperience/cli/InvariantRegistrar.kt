package social.xperience.cli

import com.google.auto.service.AutoService
import org.jetbrains.kotlin.codegen.extensions.ClassBuilderInterceptorExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import social.xperience.k1.InvariantClassGenerationInterceptor

@OptIn(ExperimentalCompilerApi::class)
@AutoService(CompilerPluginRegistrar::class)
class InvariantRegistrar : CompilerPluginRegistrar() {
    override val supportsK2: Boolean = false

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        if (!configuration.get(InvariantConfigurationKeys.KEY_ENABLED, true)) {
            return
        }
        registerComponents(this)
    }

    companion object {
        fun registerComponents(extensionStorage: ExtensionStorage) =
            with(extensionStorage) {
                ClassBuilderInterceptorExtension.registerExtension(InvariantClassGenerationInterceptor())
                // FirExtensionRegistrarAdapter.registerExtension(FirInvariantRegistrar())
            }
    }
}