import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val mavenRepository: String by project
val mavenReleasesRepository: String by project

plugins {
    kotlin("jvm") version "1.6.21"
    kotlin("plugin.spring") version "1.5.31"
    id("com.taktik.gradle.git-version") version "2.0.13-gd2de854853"
    id("com.taktik.gradle.helm-repository") version "0.2.24-g9f69211aad"
    id("org.springframework.boot") version "2.6.2"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
}

repositories {
    mavenCentral()
    maven { url = uri("https://maven.taktik.be/content/groups/public") }
}

val repoUsername: String by project
val repoPassword: String by project

val kotlinCoroutinesVersion = "1.6.4"

val gitVersion: String? by project

group = "io.icure"
version = gitVersion ?: "0.0.1-SNAPSHOT"

val compileKotlin: KotlinCompile by tasks

compileKotlin.kotlinOptions {
    languageVersion = "1.5"
    jvmTarget = "17"
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation("be.fgov.ehealth:fhir-visualization-tool:1.16")
    implementation("ca.uhn.hapi.fhir:hapi-fhir-structures-r4:6.10.3")
    implementation("ca.uhn.hapi.fhir:hapi-fhir-base:6.10.3")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$kotlinCoroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")

    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework:spring-context-support")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    implementation("com.fasterxml.jackson.core:jackson-core:2.16.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.16.1")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.16.1")

    implementation("com.squareup.okhttp3:okhttp:4.10.0")

    implementation(group = "com.github.ben-manes.caffeine", name = "caffeine", version = "3.1.5")

    // Logging
    implementation(group = "ch.qos.logback", name = "logback-classic", version = "1.2.10")
    implementation(group = "ch.qos.logback", name = "logback-access", version = "1.2.10")

    // Swagger
    implementation(group = "org.springdoc", name = "springdoc-openapi-webflux-ui", version = "1.7.0")
    implementation(group = "org.springdoc", name = "springdoc-openapi-kotlin", version = "1.7.0")

}
tasks.withType<Test> {
    useJUnitPlatform()
}
