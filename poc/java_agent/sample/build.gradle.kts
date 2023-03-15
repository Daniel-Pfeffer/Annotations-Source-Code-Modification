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

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "social.xperience.sample.MainKt"
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    val contents = configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }
    from(contents)
}