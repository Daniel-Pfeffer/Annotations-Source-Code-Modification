plugins {
    buildsrc.convention.subproject
    buildsrc.convention.`kotlin-jvm`
    kotlin("kapt")
}

description = "Kotlin compiler plugin for Function Trace PoC"

dependencies {
    implementation(kotlin("compiler-embeddable"))

    compileOnly("com.google.auto.service:auto-service:1.0.1")
    kapt("com.google.auto.service:auto-service:1.0.1")
}