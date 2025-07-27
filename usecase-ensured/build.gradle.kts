plugins {
    kotlin("jvm")
    `java-library`
    signing
    distribution
    `maven-publish`
}

version = "0.0.1"
group = "io.github.usecase-ensured"
repositories {
    mavenCentral()
    mavenLocal()
}


dependencies {
    // this is not just a test dependency since we are building on top of
    // JUnit here.
    implementation("io.rest-assured:rest-assured:5.5.5")
    implementation("org.junit.jupiter:junit-jupiter:5.13.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.19.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.19.0")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(23)
    }
    withSourcesJar()
    withJavadocJar()
}

publishing {

    repositories {
        maven {
            name = "ossrh-staging-api"
            url = uri("https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/")
            val uname: String? by project
            val pwd: String? by project
            credentials {
                username = uname
                password = pwd
            }
        }
    }
    publications {
        create<MavenPublication>("usecase-ensured") {
            groupId = group as String
            artifactId = name
            version = "0.0.1"

            from(components["java"])
            pom {
                name = "Usecase Ensured"
                description = "Pending description"
                url = "https://github.com/usecase-ensured/usecase-ensured"

                licenses {
                    license {
                        name = "MIT License"
                        url = "https://mit-license.org/"
                    }
                }
                developers {
                    developer {
                        id = "nazaratius"
                    }
                }
                scm {
                    connection = "scm:git:https://github.com/usecase-ensured/usecase-ensured.git"
                    url = "https://github.com/usecase-ensured/usecase-ensured"
                }
            }
        }
    }
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)

    sign(publishing.publications["usecase-ensured"])
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
