import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    buildsrc.convention.subproject
    buildsrc.convention.`kotlin-jvm`
    application
}

description = "Kotlin sample for Function Trace PoC"

dependencies {
    implementation(projects.annotation)
    implementation(projects.kotlinPlugin)
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xplugin=${project(":kotlin-plugin").buildDir}/libs/kotlin-plugin-$version.jar")
    }
}