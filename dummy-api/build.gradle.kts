plugins {
    kotlin("jvm") version "2.1.20"
    id("org.springframework.boot") version "3.5.3"
    kotlin("plugin.spring") version "2.1.20"
    application

}

group = "progressive-testing"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web:3.5.3")

    testImplementation(project(":lib"))
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.5.3")
    testImplementation("org.junit.jupiter:junit-jupiter:5.13.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(23)
    }
}
tasks.test {
    useJUnitPlatform()
}