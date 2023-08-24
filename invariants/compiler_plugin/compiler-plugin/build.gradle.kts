import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    buildsrc.convention.subproject
    buildsrc.convention.`kotlin-jvm`
    kotlin("kapt")
    `maven-publish`
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

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs = listOf(
            "-Xcontext-receivers"
        )
    }
}

tasks.test {
    useJUnitPlatform()
}


publishing {
    publications {
        create<MavenPublication>("default") {
            artifactId = "invariants-compiler-plugin"
            from(components["java"])

            pom {
                name.set("invariants-compiler-plugin")
                description.set("Compiler plugin for Invariants")
                url.set("https://example.com")

                licenses {
                    license {
                        name.set("Apache License 2.0")
                    }
                }
                scm {
                    url.set("https://github.com/Daniel-Pfeffer/Annotations-Source-Code-Modification")
                    connection.set("scm:git:git://github.com/Daniel-Pfeffer/Annotations-Source-Code-Modification")
                }
                developers {
                    developer {
                        name.set("Daniel Pfeffer")
                        url.set("https://github.com/Daniel-Pfeffer")
                    }
                }
            }
        }
    }
}