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
    val aisBatchLambdaShadowJarTask = tasks.getByPath(":ais-batch-lambda:shadowJar")
    val sparkJobShadowJarTask = tasks.getByPath(":spark-job:shadowJar")

    dependsOn(aisBatchLambdaShadowJarTask)

    dependsOn(sparkJobShadowJarTask)

    environmentVariables(
            "TF_VAR_AIS_BATCH_LAMBDA_JAR_PATH" to aisBatchLambdaShadowJarTask.outputs.files.singleFile.absolutePath,
            "TF_VAR_SPARK_JOB_JAR_PATH" to sparkJobShadowJarTask.outputs.files.singleFile.absolutePath,
            "TF_VAR_SPARK_JOB_JAR_NAME" to sparkJobShadowJarTask.outputs.files.singleFile.name
    )
}

afterEvaluate {
    tasks.getByName("spotlessTerraform").apply { dependsOn("terraformDownload") }
}
