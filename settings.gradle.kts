rootProject.name = "progressive-testing"
include("lib", "dummy-api")

pluginManagement {
    plugins {
        // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
        kotlin("jvm") version "2.1.20"
        id("org.springframework.boot") version "3.5.3"
        kotlin("plugin.spring") version "2.1.20"
    }
}