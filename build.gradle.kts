import com.diffplug.gradle.spotless.SpotlessExtension

plugins {
    id("com.diffplug.gradle.spotless")
    id("terraform")
}

allprojects {
    apply { plugin("com.diffplug.gradle.spotless") }

    group = "uk.gov.ukho"
    version = "1.1-SNAPSHOT"

    configure<SpotlessExtension> {
        kotlinGradle {
            ktlint()
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

    format("terraform") {
        target("src/main/terraform/**/*.tf")
        custom("terraform") { fileContents ->
            execTerraform {
                args("fmt", "-")
                stdin(fileContents)
            }
        }
    }

    kotlin {
        target("buildSrc/src/main/kotlin/**/*.kt")
        ktlint()
    }
}

afterEvaluate {
    tasks.getByName("spotlessTerraformCheck").dependsOn("downloadTerraform")
    tasks.getByName("spotlessTerraformApply").dependsOn("downloadTerraform")
}
