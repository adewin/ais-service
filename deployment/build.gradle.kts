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

    dependsOn(
            aisBatchLambdaShadowJarTask
    )

    environmentVariables(
            "TF_VAR_AIS_BATCH_LAMBDA_JAR" to aisBatchLambdaShadowJarTask.outputs.files.singleFile.absolutePath
    )
}

afterEvaluate {
    tasks.getByName("spotlessTerraform").apply { dependsOn("terraformDownload") }
}
