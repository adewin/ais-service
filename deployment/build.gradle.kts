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
    val ingestUploadFileLambdaShadowJarTask = tasks.getByPath(":lambdas:ingest-upload-file-lambda:shadowJar")
    val sparkJobShadowJarTask = tasks.getByPath(":spark-job:shadowJar")

    dependsOn(aisBatchLambdaShadowJarTask,
            sparkJobShadowJarTask,
            ingestUploadFileLambdaShadowJarTask)

    environmentVariables(
            "TF_VAR_AIS_BATCH_FUNCTION_JAR_PATH" to aisBatchLambdaShadowJarTask.outputs.files.singleFile.absolutePath,
            "TF_VAR_DATA_FILE_FUNCTION_LAMBDA_JAR_PATH" to ingestUploadFileLambdaShadowJarTask.outputs.files.singleFile.absolutePath,
            "TF_VAR_SPARK_JOB_JAR_PATH" to sparkJobShadowJarTask.outputs.files.singleFile.absolutePath,
            "TF_VAR_SPARK_JOB_JAR_NAME" to sparkJobShadowJarTask.outputs.files.singleFile.name
    )
}

afterEvaluate {
    tasks.getByName("spotlessTerraform").apply { dependsOn("terraformDownload") }
}
