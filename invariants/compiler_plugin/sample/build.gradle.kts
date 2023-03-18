import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    buildsrc.convention.subproject
    buildsrc.convention.`kotlin-jvm`
    application
}

dependencies {
    implementation(projects.invariant)
    implementation(projects.compilerPlugin)
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xplugin=${project(":compiler-plugin").buildDir}/libs/compiler-plugin-$version.jar")
    }
}