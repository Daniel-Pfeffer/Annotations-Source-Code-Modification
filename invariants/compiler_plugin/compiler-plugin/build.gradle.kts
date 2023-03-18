plugins {
    buildsrc.convention.subproject
    buildsrc.convention.`kotlin-jvm`
    kotlin("kapt")
}

description = "Kotlin Compiler Plugin for Invariants"

dependencies {
    implementation(kotlin("compiler-embeddable"))
    implementation(projects.invariant)

    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.5.0")
    // https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter-api
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testImplementation(projects.invariant)
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")

    compileOnly("com.google.auto.service:auto-service:1.0.1")
    kapt("com.google.auto.service:auto-service:1.0.1")
}

tasks.test {
    useJUnitPlatform()
}