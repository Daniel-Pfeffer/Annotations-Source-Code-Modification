plugins {
    buildsrc.convention.subproject
    buildsrc.convention.`kotlin-jvm`
    `maven-publish`
}

description = "All required Annotations/Classes for Invariants"


publishing {
    publications {
        create<MavenPublication>("default") {
            from(components["java"])
            artifactId = "invariants-annotation"


            pom {
                name.set("Invariants Annotation")
                description.set("Invariants api")
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