rootProject.name = "usecase-ensured"
include("usecase-ensured", "dummy-api")
pluginManagement {
    plugins {
        // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
        kotlin("jvm") version "2.1.20" apply false
        id("org.springframework.boot") version "3.5.3" apply false
        kotlin("plugin.spring") version "2.1.20" apply false
    }
}