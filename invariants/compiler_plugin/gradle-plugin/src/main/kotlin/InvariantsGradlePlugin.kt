package social.xperience

import org.gradle.api.provider.Provider
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry
import org.jetbrains.kotlin.gradle.plugin.*
import javax.inject.Inject

class InvariantsGradlePlugin @Inject internal constructor(private val registry: ToolingModelBuilderRegistry) :
    KotlinCompilerPluginSupportPlugin {
    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project
        return project.provider {
            val extension = project.extensions.getByType(InvariantsExtension::class.java)

            listOf(SubpluginOption("enabled", extension.enabled.toString()))
        }
    }

    override fun getCompilerPluginId(): String = "Invariants"

    override fun getPluginArtifact(): SubpluginArtifact =
        SubpluginArtifact("social.xperience", "invariants-kotlin-plugin", "0.0.1")

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean =
        kotlinCompilation.platformType == KotlinPlatformType.jvm || kotlinCompilation.platformType == KotlinPlatformType.androidJvm

}

data class InvariantsExtension(val enabled: Boolean)