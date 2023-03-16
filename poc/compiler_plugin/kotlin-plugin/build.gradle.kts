plugins {
    buildsrc.convention.subproject
    buildsrc.convention.`kotlin-jvm`
    kotlin("kapt")
    idea
}

description = "Kotlin compiler plugin for Function Trace PoC"

idea {
    module {
        isDownloadJavadoc = true
    }
}

dependencies {
    implementation(kotlin("compiler-embeddable"))

    compileOnly("com.google.auto.service:auto-service:1.0.1")
    kapt("com.google.auto.service:auto-service:1.0.1")
}