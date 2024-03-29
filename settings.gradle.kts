pluginManagement {
    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "com.diffplug.gradle.spotless" -> useVersion(Versions.spotless)
                "com.github.johnrengelman.shadow" -> useVersion(Versions.shadow)
                "cz.augi.gradle.wartremover" -> useVersion(Versions.wartremover)
                "org.owasp.dependencycheck" -> useVersion(Versions.dependencyCheckGradle)
                "com.github.spotbugs" -> useVersion(Versions.spotbugs)
                "org.springframework.boot" -> useVersion(Versions.springBoot)
                "com.google.cloud.tools.jib" -> useVersion(Versions.googleJib)
            }
        }
    }
}

rootProject.name = "ais-service"

include("deployment",
        "heatmapGenerator",
        "heatmapAggregator",
        "lambdas:ingest-upload-file-lambda",
        "lambdas:validate-new-job-config-lambda",
        "lambdas:invoke-step-function-lambda",
        "lambdas:handle-heatmap-outcome-lambda",
        "lambdas:ingest-sql-file",
        "lambdas:libraries:s3-event-handling",
        "lambdas:libraries:s3-test-util",
        "lambdas:libraries:emr-job-runner",
        "lambdas:libraries:heatmap-job-model",
        "lambdas:trigger-raw-partitioning-lambda",
        "spark:libraries:spark-job",
        "spark:libraries:ais-schemata",
        "spark:libraries:test-support",
        "spark:partition-raw-ais",
        "lambdas:process_new_static_file")
