plugins {
    application
    buildsrc.convention.subproject
    buildsrc.convention.`kotlin-jvm`
}

description = "Kotlin Agent for Function Trace PoC"

dependencies {
    // https://mvnrepository.com/artifact/org.slf4j/slf4j-api
    implementation("org.slf4j:slf4j-api:2.0.6")
    // https://mvnrepository.com/artifact/org.javassist/javassist
    implementation("org.javassist:javassist:3.29.2-GA")

}

tasks.withType<Jar>().configureEach {
    manifest {
        attributes(
            mapOf(
                "Manifest-Version" to 1.0,
                "Agent-Class" to "InstrumentationAgent",
                "Can-Redefine-Classes" to true,
                "Can-Retransform-Classes" to true,
                "Premain-Class" to "InstrumentationAgent"
            )
        )
    }
}