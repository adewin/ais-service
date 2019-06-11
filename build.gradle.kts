
import com.diffplug.gradle.spotless.SpotlessExtension

plugins {
    id("com.diffplug.gradle.spotless")
}

allprojects {
    apply { plugin("com.diffplug.gradle.spotless") }

    group = "uk.gov.ukho"
    version = "1.2-SNAPSHOT"

    configure<SpotlessExtension> {
        kotlinGradle {
            ktlint()
        }
    }

    if (plugins.hasPlugin("java")) {
        apply { plugin("org.gradle.checkstyle") }

        configure<SpotlessExtension> {
            java {
                googleJavaFormat()
                indentWithSpaces(4)
            }
        }
    }
}

configure<SpotlessExtension> {
    format("markdown") {
        target("README.md")
        trimTrailingWhitespace()
        indentWithSpaces()
        endWithNewline()
    }

    format("yml") {
        target(".circleci/config.yml", "azure-pipelines.yml")
        trimTrailingWhitespace()
        indentWithSpaces(2)
        endWithNewline()
    }

    kotlin {
        target("buildSrc/src/main/kotlin/**/*.kt")
        ktlint()
    }
}
