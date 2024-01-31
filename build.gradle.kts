import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.31"
    kotlin("plugin.spring") version "1.5.31"
    id("org.springframework.boot") version "2.7.11"
    id("io.spring.dependency-management") version "1.0.15.RELEASE"
    id("cz.augi.docker-java") version "3.1.0"
    id("com.palantir.git-version") version "0.15.0"
}

buildscript {

    repositories {
        mavenCentral()
		maven { url = uri("file://C:/Users/eh068/Documents/LaboResultVisualizer/laboResultVisualization2/target/mvn-repo") }
        //maven { url = uri("https://ehealthplatformstandards.github.io/laboResultVisualization/") }
        //maven { url = uri("file://C:/Temp/toy/laboResultVisualization/target/mvn-repo") }
    }

}

val repoPassword: String by project
val mavenReleasesRepository: String by project

val kotlinCoroutinesVersion = "1.6.4"

val gitVersion: groovy.lang.Closure<String> by extra
version = gitVersion()
group = "be.fgov.ehealth"


java.sourceCompatibility = JavaVersion.VERSION_11

apply(plugin = "cz.augi.docker-java")


configure<cz.augi.gradle.dockerjava.DockerJavaExtension> {
        image = "be.fgov.ehealth/ehealth-fhirviz:"+version // name of the resulting Docker image; mandatory
        customDockerfile = file("Dockerfile")
        setPorts(8912)  // list of exposed ports; default: empty
}


val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    languageVersion = "1.5"
    jvmTarget = "11"
}

repositories {
    mavenCentral()
	maven { url = uri("file://C:/Users/eh068/Documents/LaboResultVisualizer/laboResultVisualization2/target/mvn-repo") }
    //maven { url = uri("file://C:/Temp/toy/laboResultVisualization/target/mvn-repo") }
    //maven { url = uri("https://ehealthplatformstandards.github.io/laboResultVisualization") }
    /*
    maven {
        credentials {
            username = extra["repoUsername"].toString()
            password = extra["repoPassword"].toString()
        }
        url = uri(extra["mavenRepository"].toString())
    }
    */
}

dependencies {
    implementation("be.fgov.ehealth:fhir-visualization-tool:1.13")
    implementation("ca.uhn.hapi.fhir:hapi-fhir-structures-r4:6.10.3")
    implementation("ca.uhn.hapi.fhir:hapi-fhir-base:6.10.3")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$kotlinCoroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")

    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework:spring-context-support")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.15.0")
    implementation("com.fasterxml.jackson.core:jackson-core:2.16.0-rc1")

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
