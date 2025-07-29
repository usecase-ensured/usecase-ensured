plugins {
    `java-library`
    signing
    `maven-publish`
    distribution
}

val projectVersion = "0.0.1"

version = projectVersion
group = "io.github.usecase-ensured"
repositories {
    mavenCentral()
    mavenLocal()
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

dependencies {
    // this is not just a test dependency since we are building on top of JUnit here.
    implementation("io.rest-assured:rest-assured:5.5.5")
    implementation("org.junit.jupiter:junit-jupiter:5.13.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.19.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.19.0")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(23)
    }
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("usecase-ensured") {
            groupId = group as String
            artifactId = name
            version = projectVersion

            from(components["java"])
            pom {
                name = "Usecase Ensured"
                description = "Pending description"
                url = "https://github.com/usecase-ensured/usecase-ensured"

                licenses {
                    license {
                        name = "MIT License"
                        url = "https://opensource.org/licenses/MIT"
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

private val artifactDirectory = layout.buildDirectory.dir("release/artifacts").get()

tasks.register<Copy>("createArtifactDirectory") {
    dependsOn(tasks["publishToMavenLocal"])
    group = "internal"
    destinationDir = artifactDirectory.asFile
    into(".") {
        from("build/libs") {
            include("*.jar", "*.jar.asc")
        }
        from("build/publications/usecase-ensured") {
            rename("(.*)\\.xml(.*)", "${project.name}-$version.pom$2")
        }
        exclude("**/module.json*")
    }
}

tasks.register<Exec>("sha1Fingerprint") {
    dependsOn(tasks["createArtifactDirectory"])
    group = "internal"
    workingDir(artifactDirectory)

    val files = "ls *.jar *.pom"
    val fingerprint = "xargs -I % bash -c \"sha1sum \\\$0 | awk '{print \\$1}' > \\\$0.sha1\" %"
    val sha1Command = "$files | $fingerprint"

    commandLine("sh", "-c", sha1Command)
}

tasks.register<Exec>("md5Fingerprint") {
    dependsOn(tasks["createArtifactDirectory"])
    group = "internal"
    workingDir(artifactDirectory)

    val files = "ls *.jar *.pom"
    val fingerprint = "xargs -I % bash -c \"md5sum \\\$0 | awk '{print \\$1}' > \\\$0.md5\" %"
    val md5Command = "$files | $fingerprint"

    commandLine("sh", "-c", md5Command)
}

tasks.register<Zip>("zipRelease") {
    group = "release"

    val previousSteps = arrayOf(
        tasks["clean"],
        tasks["sha1Fingerprint"],
        tasks["md5Fingerprint"]
    )
    dependsOn(*previousSteps)

    val zipArchiveWithMavenRepoLayout = "io/github/usecase-ensured/usecase-ensured/$projectVersion"

    archiveFileName = "${project.name}-$projectVersion.zip"
    destinationDirectory = layout.buildDirectory.dir("release")
    from(artifactDirectory)
    into(zipArchiveWithMavenRepoLayout)
}
