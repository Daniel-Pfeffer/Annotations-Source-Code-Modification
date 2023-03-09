plugins {
    application
    buildsrc.convention.subproject
    buildsrc.convention.`kotlin-jvm`
}

description = "Kotlin sample for Function Trace PoC"

dependencies {
    implementation(project(":annotation"))
}