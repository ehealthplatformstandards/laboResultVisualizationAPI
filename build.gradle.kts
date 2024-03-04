import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val mavenRepository: String by project

plugins {
    kotlin("jvm") version "1.5.31"
    kotlin("plugin.spring") version "1.5.31"
    id("com.taktik.gradle.git-version") version "2.0.5-g16ba274290"
    id("org.springframework.boot") version "2.6.2"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
}

buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://maven.taktik.be/content/groups/public") }
        maven { url = uri("https://repo.spring.io/plugins-release") }
    }
    dependencies {
        classpath("com.taktik.gradle:gradle-plugin-helm-repository:0.2.20-7b38909679")
    }

}

repositories {
    mavenCentral()
    maven { url = uri("https://maven.taktik.be/content/groups/public") }
    maven { url = uri("https://www.e-contract.be/maven2/") }
    maven { url = uri("https://repo.ehealth.fgov.be/artifactory/maven2/") }
}

val repoUsername: String by project
val repoPassword: String by project

val kotlinCoroutinesVersion = "1.6.4"

val gitVersion: String? by project

group = "io.icure"
version = gitVersion ?: "0.0.1-SNAPSHOT"

java.sourceCompatibility = JavaVersion.VERSION_17

apply(plugin = "helm-repository")

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    languageVersion = "1.5"
    jvmTarget = "17"
}

repositories {
    mavenCentral()
    maven {
        credentials {
            username = extra["repoUsername"].toString()
            password = extra["repoPassword"].toString()
        }
        url = uri(extra["mavenRepository"].toString())
    }
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

    implementation(group = "com.github.ben-manes.caffeine", name = "caffeine", version = "3.1.5")

    // Logging
    implementation(group = "ch.qos.logback", name = "logback-classic", version = "1.2.10")
    implementation(group = "ch.qos.logback", name = "logback-access", version = "1.2.10")

    // Swagger
    implementation(group = "org.springdoc", name = "springdoc-openapi-webflux-ui", version = "1.7.0")
    implementation(group = "org.springdoc", name = "springdoc-openapi-kotlin", version = "1.7.0")

}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
