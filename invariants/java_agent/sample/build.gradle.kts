plugins {
    application
    buildSrc.convention.subproject
    buildSrc.convention.`kotlin-jvm`
}

description = "Kotlin sample for Function Trace PoC"

dependencies {
    implementation(project(":invariant"))
    implementation(project(":agent"))
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "social.xperience.sample.MainKt"
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    val contents = configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }
    from(contents)
}