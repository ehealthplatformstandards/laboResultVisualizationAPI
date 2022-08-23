import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.31"
    kotlin("plugin.spring") version "1.5.31"
    id("org.springframework.boot") version "2.6.2"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
}

buildscript {
    repositories {
        mavenCentral()
        maven { url = uri("https://maven.taktik.be/content/groups/public") }
    }
    dependencies {
        classpath("com.taktik.gradle:gradle-plugin-docker-java:2.1.1")
        classpath("com.taktik.gradle:gradle-plugin-git-version:2.0.2")
        classpath("com.taktik.gradle:gradle-plugin-helm-repository:0.2.20-7b38909679")
    }
}

val repoUsername: String by project
val repoPassword: String by project
val mavenReleasesRepository: String by project

val kotlinCoroutinesVersion = "1.5.2"

apply(plugin = "git-version")

val gitVersion: String by project
group = "io.icure"
version = gitVersion

java.sourceCompatibility = JavaVersion.VERSION_11

apply(plugin = "docker-java")
apply(plugin = "helm-repository")

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    languageVersion = "1.5"
    jvmTarget = "11"
}

configure<com.taktik.gradle.plugins.flowr.DockerJavaPluginExtension> {
    imageRepoAndName = "taktik/ehealth-fhirviz"

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
    implementation("be.fgov.ehealth:fhir-visualization-tool:1.11")
    implementation("io.icure:async-jackson-http-client:0.1.4-6cab16ec6e")
    implementation("ca.uhn.hapi.fhir:hapi-fhir-structures-r4:4.1.0")
    implementation("ca.uhn.hapi.fhir:hapi-fhir-base:4.1.0")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$kotlinCoroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")

    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework:spring-context-support")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.12.5")

    implementation(group = "com.github.ben-manes.caffeine", name = "caffeine", version = "3.0.4")

    // Logging
    implementation(group = "ch.qos.logback", name = "logback-classic", version = "1.2.10")
    implementation(group = "ch.qos.logback", name = "logback-access", version = "1.2.10")
    implementation(group = "com.taktik.boot", name = "spring-boot-starter-gke-logging", version = "2.1.174-0f038f8004")

    // Swagger
    implementation(group = "org.springdoc", name = "springdoc-openapi-webflux-ui", version = "1.5.11")
    implementation(group = "org.springdoc", name = "springdoc-openapi-kotlin", version = "1.5.11")

    implementation(group = "com.github.ben-manes.caffeine", name = "caffeine", version = "3.0.3")
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
