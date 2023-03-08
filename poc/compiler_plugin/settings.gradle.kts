rootProject.name = "FunctionTrace-CompilerPlugin"

include(":gradle-plugin")
include(":kotlin-plugin")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

apply(from = "./buildSrc/repositories.settings.gradle.kts")

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
}
include("sample")
include("annotation")
