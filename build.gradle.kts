
import com.diffplug.gradle.spotless.SpotlessExtension
import org.owasp.dependencycheck.gradle.extension.DependencyCheckExtension

plugins {
    id("com.diffplug.gradle.spotless")
    id("org.owasp.dependencycheck") apply false
}

allprojects {
    apply { plugin("com.diffplug.gradle.spotless") }

    group = "uk.gov.ukho"
    version = "1.7-SNAPSHOT"

    configure<SpotlessExtension> {
        kotlinGradle {
            ktlint()
        }
    }

    pluginManager.withPlugin("java") {
        apply(plugin = "org.owasp.dependencycheck")

        configure<SpotlessExtension> {
            java {
                removeUnusedImports()
                googleJavaFormat()
                indentWithSpaces(4)
            }
        }

        configure<DependencyCheckExtension> {
            failBuildOnCVSS = 0.0f
            scanConfigurations = listOf("implementation")
        }

        tasks.getByName("check").dependsOn("dependencyCheckAnalyze")
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
