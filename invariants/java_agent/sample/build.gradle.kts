plugins {
    application
    buildSrc.convention.subproject
    buildSrc.convention.`kotlin-jvm`
}

description = "Kotlin sample for Function Trace PoC"

dependencies {
    implementation(project(":invariant"))
    implementation(project(":agent"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "social.xperience.sample.MainKt"
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    val contents = configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }
    from(contents)
}


tasks.test {
    useJUnitPlatform()
    jvmArgs =
        listOf("-javaagent:/home/danielpfeffer/Documents/jku/thesis/bachelor/development/invariants/java_agent/agent/build/libs/agent-1.0-SNAPSHOT.jar")
}