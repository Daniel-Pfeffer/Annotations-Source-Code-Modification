package social.xperience

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry
import org.jetbrains.kotlin.gradle.plugin.*
import javax.inject.Inject

class FunctionTraceGradlePlugin @Inject internal constructor(private val registry: ToolingModelBuilderRegistry) :
    KotlinCompilerPluginSupportPlugin {

    override fun apply(target: Project) {
        target.extensions.create(getCompilerPluginId(), FunctionTraceExtension::class.java)
    }

    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project

        return project.provider {
            val extension = project.extensions.getByType(FunctionTraceExtension::class.java)

            val enabled = SubpluginOption("enabled", extension.enabled.toString())
            val annotations = extension.annotations.map { SubpluginOption("annotation", it) }
            annotations + enabled
        }
    }

    override fun getCompilerPluginId(): String = "FunctionTrace"

    override fun getPluginArtifact(): SubpluginArtifact =
        SubpluginArtifact("social.xperience", "function-trace-kotlin-plugin", "0.0.1")

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean =
        kotlinCompilation.platformType == KotlinPlatformType.jvm || kotlinCompilation.platformType == KotlinPlatformType.androidJvm
}

data class FunctionTraceExtension(
    val enabled: Boolean = true,
    val annotations: List<String> = listOf("social.xperience.FunctionTrace.Trace"),
)