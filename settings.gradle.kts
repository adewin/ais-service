pluginManagement {
    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "com.diffplug.gradle.spotless" -> useVersion(Versions.spotless)
                "com.github.johnrengelman.shadow" -> useVersion(Versions.shadow)
                "cz.augi.gradle.wartremover" -> useVersion(Versions.wartremover)
            }
        }
    }
}

rootProject.name = "ais-to-raster"

include("spark-job")
