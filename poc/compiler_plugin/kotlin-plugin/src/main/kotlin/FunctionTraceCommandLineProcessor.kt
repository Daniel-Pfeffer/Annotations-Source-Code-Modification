import com.google.auto.service.AutoService
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

@OptIn(ExperimentalCompilerApi::class)
@AutoService(CommandLineProcessor::class)
class FunctionTraceCommandLineProcessor : CommandLineProcessor {
    override val pluginId: String = "FunctionTrace"
    override val pluginOptions: Collection<AbstractCliOption> = listOf(
        CliOption(
            KeyOptions.KEY_ANNOTATIONS.toString(),
            "<fqname>",
            "FunctionTrace annotation",
            required = false,
            allowMultipleOccurrences = true
        ),
        CliOption(
            KeyOptions.KEY_ENABLED.toString(),
            "<true|false>",
            "Whether the plugin is enabled",
            false
        )
    )

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
        when (option.optionName) {
            KeyOptions.KEY_ENABLED.toString() -> configuration.put(KeyOptions.KEY_ENABLED, value.toBoolean())
            KeyOptions.KEY_ANNOTATIONS.toString() -> configuration.appendList(KeyOptions.KEY_ANNOTATIONS, value)
            else -> error("Unexpected config options ${option.optionName}")
        }
    }
}

object KeyOptions {
    val KEY_ENABLED = CompilerConfigurationKey.create<Boolean>("enabled")
    val KEY_ANNOTATIONS = CompilerConfigurationKey.create<List<String>>("annotations")
}