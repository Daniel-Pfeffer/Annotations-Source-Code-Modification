plugins {
    buildsrc.convention.subproject
    buildsrc.convention.`kotlin-jvm`
    kotlin("kapt")
    `java-gradle-plugin`
}

group = "social.xperience"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    gradlePluginPortal()
}

gradlePlugin {
    plugins {
        create("function_trace") {
            id = "social.xperience.function-trace"
            implementationClass = "social.xperience.FunctionTraceGradlePlugin"
        }
    }
}

dependencies {
    implementation(kotlin("gradle-plugin-api"))

    compileOnly("com.google.auto.service:auto-service:1.0.1")
    kapt("com.google.auto.service:auto-service:1.0.1")
}
