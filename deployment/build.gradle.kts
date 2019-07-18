import com.diffplug.gradle.spotless.SpotlessExtension

plugins {
    terraform
}

configure<SpotlessExtension> {
    format("terraform") {
        target("src/main/terraform/**/*.tf")
        custom("terraform") { fileContents ->
            terraformExec {
                args("fmt", "-")
                stdin(fileContents)
            }
        }
    }
}

configure<TerraformExtension> {

    val aisBatchLambdaShadowJarTask = tasks.getByPath(":lambdas:ais-batch-lambda:shadowJar")
    val aisRawParititioningSparkJobShadowJarTask = tasks.getByPath(":spark:partition-raw-ais:shadowJar")
    val aisResamplingSparkJobShadowJarTask = tasks.getByPath(":spark:resample-ais:shadowJar")
    val ingestUploadFileLambdaShadowJarTask = tasks.getByPath(":lambdas:ingest-upload-file-lambda:shadowJar")
    val triggerRawPartitioningLambdaShadowJarTask = tasks.getByPath(":lambdas:trigger-raw-partitioning-lambda:shadowJar")
    val triggerResampleLambdaShadowJarTask = tasks.getByPath(":lambdas:trigger-resample-lambda:shadowJar")
    val oldSparkJobShadowJarTask = tasks.getByPath(":spark:old-spark-job:shadowJar")

    dependsOn(aisBatchLambdaShadowJarTask,
            oldSparkJobShadowJarTask,
            ingestUploadFileLambdaShadowJarTask,
            triggerRawPartitioningLambdaShadowJarTask,
            triggerResampleLambdaShadowJarTask,
            aisRawParititioningSparkJobShadowJarTask,
            aisResamplingSparkJobShadowJarTask)

    environmentVariables(
            "TF_VAR_AIS_BATCH_FUNCTION_JAR_PATH" to aisBatchLambdaShadowJarTask.outputs.files.singleFile.absolutePath,
            "TF_VAR_DATA_FILE_FUNCTION_LAMBDA_JAR_PATH" to ingestUploadFileLambdaShadowJarTask.outputs.files.singleFile.absolutePath,
            "TF_VAR_TRIGGER_RAW_PARTITION_FUNCTION_LAMBDA_JAR_PATH" to triggerRawPartitioningLambdaShadowJarTask.outputs.files.singleFile.absolutePath,
            "TF_VAR_OLD_SPARK_JOB_JAR_PATH" to oldSparkJobShadowJarTask.outputs.files.singleFile.absolutePath,
            "TF_VAR_OLD_SPARK_JOB_JAR_NAME" to oldSparkJobShadowJarTask.outputs.files.singleFile.name,
            "TF_VAR_PARTITIONING_SPARK_JOB_JAR_NAME" to aisRawParititioningSparkJobShadowJarTask.outputs.files.singleFile.name,
            "TF_VAR_PARTITIONING_SPARK_JOB_JAR_PATH" to aisRawParititioningSparkJobShadowJarTask.outputs.files.singleFile.absolutePath,
            "TF_VAR_RESAMPLING_SPARK_JOB_JAR_NAME" to aisResamplingSparkJobShadowJarTask.outputs.files.singleFile.name,
            "TF_VAR_RESAMPLING_SPARK_JOB_JAR_PATH" to aisResamplingSparkJobShadowJarTask.outputs.files.singleFile.absolutePath,
            "TF_VAR_TRIGGER_RESAMPLE_FUNCTION_LAMBDA_JAR_PATH" to triggerResampleLambdaShadowJarTask.outputs.files.singleFile.absolutePath
    )
}

afterEvaluate {
    tasks.getByName("spotlessTerraform").apply { dependsOn("terraformDownload") }
}
