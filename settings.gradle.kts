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

include("deployment",
        "lambdas:ais-batch-lambda",
        "lambdas:ingest-upload-file-lambda",
        "lambdas:libraries:s3-event-handling",
        "lambdas:libraries:s3-test-util",
        "lambdas:libraries:emr-job-runner",
        "lambdas:trigger-raw-partitioning-lambda",
        "lambdas:trigger-resample-lambda",
        "libraries:spark-job",
        "libraries:ais-schemata",
        "libraries:test-support",
        "old-spark-job",
        "partition-raw-ais")
