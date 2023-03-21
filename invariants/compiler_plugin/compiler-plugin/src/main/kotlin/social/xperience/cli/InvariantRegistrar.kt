package social.xperience.cli

import com.google.auto.service.AutoService
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import social.xperience.k2.InvariantIrGenerationExtension

@OptIn(ExperimentalCompilerApi::class)
@AutoService(CompilerPluginRegistrar::class)
class InvariantRegistrar : CompilerPluginRegistrar() {
    override val supportsK2: Boolean = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        if (!configuration.get(InvariantConfigurationKeys.KEY_ENABLED, true)) {
            return
        }
        val messageCollector = configuration.get(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)
        registerComponents(this, messageCollector)
    }

    companion object {
        fun registerComponents(extensionStorage: ExtensionStorage, messageCollector: MessageCollector) =
            with(extensionStorage) {
                IrGenerationExtension.registerExtension(InvariantIrGenerationExtension(messageCollector))
            }
    }
}