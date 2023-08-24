import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    buildsrc.convention.subproject
    buildsrc.convention.`kotlin-jvm`
    application
}

dependencies {
    implementation(projects.invariant)
    implementation(projects.compilerPlugin)

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs = listOf(
            "-Xplugin=${project(":compiler-plugin").buildDir}/libs/compiler-plugin-$version.jar",
            "-Xcontext-receivers"
        )
    }
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    sourceSets.all {
        languageSettings {
            languageVersion = "2.0"
        }
    }
}