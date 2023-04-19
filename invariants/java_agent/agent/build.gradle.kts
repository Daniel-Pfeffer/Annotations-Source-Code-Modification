plugins {
    application
    buildSrc.convention.subproject
    buildSrc.convention.`kotlin-jvm`
}

description = "Kotlin Agent for Function Trace PoC"

dependencies {
    implementation("org.apache.logging.log4j:log4j-api:2.20.0")
    implementation("org.apache.logging.log4j:log4j-core:2.20.0")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.20.0")
    implementation("org.javassist:javassist:3.29.2-GA")
    implementation(project(":invariant"))
}

tasks.withType<Jar>().configureEach {
    manifest {
        attributes(
            mapOf(
                "Manifest-Version" to 1.0,
                "Agent-Class" to "social.xperience.InstrumentationAgent",
                "Can-Redefine-Classes" to true,
                "Can-Retransform-Classes" to true,
                "Premain-Class" to "social.xperience.InstrumentationAgent"
            )
        )
    }
}