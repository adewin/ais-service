import com.diffplug.gradle.spotless.SpotlessExtension

plugins {
    id("com.diffplug.gradle.spotless")
}

allprojects {
    apply(plugin = "com.diffplug.gradle.spotless")

    group = "uk.gov.ukho"
    version = "1.1-SNAPSHOT"

    extensions.getByType<SpotlessExtension>().apply {
        kotlinGradle {
            ktlint()
        }
    }
}

extensions.getByType<SpotlessExtension>().apply {
    format("markdown") {
        target("README.md")
        trimTrailingWhitespace()
        indentWithSpaces()
        endWithNewline()
    }

    format("yml") {
        target(".circleci/config.yml")
        trimTrailingWhitespace()
        indentWithSpaces(2)
        endWithNewline()
    }
}
