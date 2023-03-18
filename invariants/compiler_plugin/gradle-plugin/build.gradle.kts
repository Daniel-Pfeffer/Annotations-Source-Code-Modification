plugins {
    buildsrc.convention.subproject
    buildsrc.convention.`kotlin-jvm`
    kotlin("kapt")
    `java-gradle-plugin`
}

gradlePlugin {
    plugins {
        create("invariants") {
            id = "social.xperience.invariants"
            implementationClass = "social.xperience.InvariantsGradlePlugin"
        }
    }
}

dependencies {
    implementation(kotlin("gradle-plugin-api"))

    compileOnly("com.google.auto.service:auto-service:1.0.1")
    kapt("com.google.auto.service:auto-service:1.0.1")
}