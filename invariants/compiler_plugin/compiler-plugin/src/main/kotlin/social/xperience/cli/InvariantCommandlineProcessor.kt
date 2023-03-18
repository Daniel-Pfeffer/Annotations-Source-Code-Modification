package social.xperience.cli

import com.google.auto.service.AutoService
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import social.xperience.common.InvariantPluginNames
import social.xperience.common.InvariantPluginNames.ENABLED_OPTION_NAME
import social.xperience.common.InvariantPluginNames.PLUGIN_ID

@OptIn(ExperimentalCompilerApi::class)
@AutoService(CommandLineProcessor::class)
class InvariantCommandlineProcessor : CommandLineProcessor {
    override val pluginId: String = PLUGIN_ID
    override val pluginOptions: Collection<AbstractCliOption> = listOf(
        CliOption(
            optionName = ENABLED_OPTION_NAME,
            valueDescription = "<true|false>",
            description = "Enable/Disable Invariants",
            required = false
        )
    )

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
        when (option.optionName) {
            InvariantConfigurationKeys.KEY_ENABLED.toString() -> configuration.put(
                InvariantConfigurationKeys.KEY_ENABLED,
                value.toBoolean()
            )

            else -> throw IllegalArgumentException("Unknown option $option")
        }
    }
}