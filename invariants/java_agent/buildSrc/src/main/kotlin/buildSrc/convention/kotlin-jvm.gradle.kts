package buildSrc.convention

plugins {
    id("buildSrc.convention.subproject")
    kotlin("jvm")
}

java {
    targetCompatibility = JavaVersion.VERSION_1_8
    sourceCompatibility = JavaVersion.VERSION_1_8
    withSourcesJar()
    withJavadocJar()
}
