plugins {
    application
    buildsrc.convention.subproject
    buildsrc.convention.`kotlin-jvm`
}

description = "Kotlin sample for Function Trace PoC"

dependencies {
    implementation(project(":annotation"))
    implementation("org.javassist:javassist:3.29.2-GA")
}