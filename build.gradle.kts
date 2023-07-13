import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.31"
    kotlin("plugin.spring") version "1.5.31"
    id("org.springframework.boot") version "2.7.11"
    id("io.spring.dependency-management") version "1.0.15.RELEASE"
    id("cz.augi.docker-java") version "3.1.0"
     id("com.palantir.git-version") version "0.15.0"
}

//docker run -p 8912:8912 be.fgov.ehealth/ehealth-fhirviz:1.0.0

buildscript {

    repositories {
        mavenCentral()
		maven { url = uri("file://C:/Users/eh068/Documents/LaboResultVisualizer/laboResultVisualization2/target/mvn-repo") }
        //maven { url = uri("https://ehealthplatformstandards.github.io/laboResultVisualization/") }
        //maven { url = uri("file://C:/Temp/toy/laboResultVisualization/target/mvn-repo") }
    }
    /*
    dependencies {
        classpath("com.taktik.gradle:gradle-plugin-docker-java:2.1.1")
        classpath("com.taktik.gradle:gradle-plugin-git-version:2.0.2")
        classpath("com.taktik.gradle:gradle-plugin-helm-repository:0.2.20-7b38909679")
    }
    */
}

//val repoUsername: String by project
val repoPassword: String by project
val mavenReleasesRepository: String by project

val kotlinCoroutinesVersion = "1.6.4"

//apply(plugin = "git-version")

val gitVersion: groovy.lang.Closure<String> by extra
version = gitVersion()
group = "be.fgov.ehealth"


java.sourceCompatibility = JavaVersion.VERSION_11

//apply(plugin = "docker-java")
//apply(plugin = "helm-repository")
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
/*
configure<com.taktik.gradle.plugins.flowr.DockerJavaPluginExtension> {
    imageRepoAndName = "taktik/ehealth-fhirviz"

}
*/
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
    //implementation("io.icure:async-jackson-http-client:0.1.4-6cab16ec6e")
    implementation("ca.uhn.hapi.fhir:hapi-fhir-structures-r4:6.6.1")
    implementation("ca.uhn.hapi.fhir:hapi-fhir-base:6.6.1")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$kotlinCoroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")

    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework:spring-context-support")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.15.0")

    implementation(group = "com.github.ben-manes.caffeine", name = "caffeine", version = "3.1.5")

    // Logging
    implementation(group = "ch.qos.logback", name = "logback-classic", version = "1.2.10")
    implementation(group = "ch.qos.logback", name = "logback-access", version = "1.2.10")

    //implementation(group = "com.taktik.boot", name = "spring-boot-starter-gke-logging", version = "2.1.174-0f038f8004")

    // Swagger
    implementation(group = "org.springdoc", name = "springdoc-openapi-webflux-ui", version = "1.7.0")
    implementation(group = "org.springdoc", name = "springdoc-openapi-kotlin", version = "1.7.0")

    implementation(group = "com.github.ben-manes.caffeine", name = "caffeine", version = "3.1.5")
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
