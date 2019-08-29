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

    val aisRawParititioningSparkJobShadowJarTask = tasks.getByPath(":spark:partition-raw-ais:shadowJar")
    val ingestUploadFileLambdaShadowJarTask = tasks.getByPath(":lambdas:ingest-upload-file-lambda:shadowJar")
    val triggerRawPartitioningLambdaShadowJarTask = tasks.getByPath(":lambdas:trigger-raw-partitioning-lambda:shadowJar")
    val processStaticDataDistZipTask = tasks.getByPath(":lambdas:process_new_static_file:pythonDistZip")

    dependsOn(ingestUploadFileLambdaShadowJarTask,
            triggerRawPartitioningLambdaShadowJarTask,
            processStaticDataDistZipTask,
            aisRawParititioningSparkJobShadowJarTask)

    environmentVariables(
            "TF_VAR_DATA_FILE_FUNCTION_LAMBDA_JAR_PATH" to ingestUploadFileLambdaShadowJarTask.outputs.files.singleFile.absolutePath,
            "TF_VAR_TRIGGER_RAW_PARTITION_FUNCTION_LAMBDA_JAR_PATH" to triggerRawPartitioningLambdaShadowJarTask.outputs.files.singleFile.absolutePath,
            "TF_VAR_PARTITIONING_SPARK_JOB_JAR_NAME" to aisRawParititioningSparkJobShadowJarTask.outputs.files.singleFile.name,
            "TF_VAR_PARTITIONING_SPARK_JOB_JAR_PATH" to aisRawParititioningSparkJobShadowJarTask.outputs.files.singleFile.absolutePath,
            "TF_VAR_PROCESS_STATIC_DATA_ZIP_PATH" to processStaticDataDistZipTask.outputs.files.singleFile.absolutePath
    )
}

afterEvaluate {
    tasks.getByName("spotlessTerraform").dependsOn("terraformDownload")
}
