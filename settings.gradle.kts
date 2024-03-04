rootProject.name = "ehealth-fhirviz"

pluginManagement {
    repositories {
        maven { url = uri("https://maven.taktik.be/content/groups/public") }
        maven { url = uri("https://repo.spring.io/plugins-release") }
        maven { url = uri("https://jitpack.io") }
        gradlePluginPortal()
        mavenCentral()
    }
}
