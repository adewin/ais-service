pluginManagement {
    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "com.diffplug.gradle.spotless" -> useVersion(Versions.spotless)
                "com.github.johnrengelman.shadow" -> useVersion(Versions.shadow)
                "cz.augi.gradle.wartremover" -> useVersion(Versions.wartremover)
                "org.owasp.dependencycheck" -> useVersion(Versions.dependencyCheckGradle)
                "com.github.spotbugs" -> useVersion(Versions.spotbugs)
            }
        }
    }
}

rootProject.name = "ais-service"

include("spark-job")
include("ais-batch-lambda")
include("deployment")
