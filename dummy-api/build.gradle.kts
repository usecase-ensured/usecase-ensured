plugins {
    kotlin("jvm")
    id("org.springframework.boot")
    kotlin("plugin.spring")
    application

}

group = "progressive-testing"
version = "0.0.1"

fun property(name: String): Any? {
    return project.findProperty(name)
}

repositories {
    mavenCentral()
    maven {
        url =
            uri(property("githubPackageRepo") as String)
        credentials {
            username = property("githubRepoUser") as String
            password = property("githubRepoPassword") as String
        }
    }
}

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:3.5.3"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")

    testImplementation(project(":lib"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
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