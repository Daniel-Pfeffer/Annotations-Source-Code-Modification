rootProject.name = "Invariants-Compiler_Plugin"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

apply(from = "./buildSrc/repositories.settings.gradle.kts")

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
}

include("gradle-plugin")
include("invariant")
include("sample")
include("compiler-plugin")