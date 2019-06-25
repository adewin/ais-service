import com.diffplug.gradle.spotless.SpotlessExtension
import com.github.spotbugs.SpotBugsExtension
import com.github.spotbugs.SpotBugsTask
import org.owasp.dependencycheck.gradle.extension.DependencyCheckExtension

plugins {
    id("com.diffplug.gradle.spotless")
    id("org.owasp.dependencycheck") apply false
    id("com.github.spotbugs")
}

allprojects {
    apply { plugin("com.diffplug.gradle.spotless") }

    group = "uk.gov.ukho"
    version = "1.9-SNAPSHOT"

    configure<SpotlessExtension> {
        kotlinGradle {
            ktlint()
        }
    }

    pluginManager.withPlugin("java") {
        apply(plugin = "org.owasp.dependencycheck")
        apply(plugin = "com.github.spotbugs")

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

        dependencies {
            spotbugsPlugins("com.h3xstream.findsecbugs:findsecbugs-plugin:${Versions.findsecbugs}")
        }

        configure<SpotBugsExtension> {
            isIgnoreFailures = false
            reportLevel = "high"
            toolVersion = "3.1.12"
        }

        tasks.withType<SpotBugsTask> {
            reports {
                xml.isEnabled = false
                html.isEnabled = true
            }
        }
    }

    tasks.register<Copy>("aggregateReports") {
        val reportDir = "${rootProject.buildDir}/reports/${project.name}"
        mkdir(reportDir)
        from("${project.buildDir}/reports/") { include("**/*") }
        into(reportDir)
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
