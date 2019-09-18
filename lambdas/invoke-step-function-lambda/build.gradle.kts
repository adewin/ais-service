import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.PropertiesFileTransformer

plugins {
    java
    id("org.springframework.boot")
}

tasks.getByName("assemble").dependsOn("shadowJar")

tasks.withType<ShadowJar> {
    isZip64 = true
    classifier = "aws"
    // Required for Spring
    mergeServiceFiles()
    append("META-INF/spring.handlers")
    append("META-INF/spring.schemas")
    append("META-INF/spring.tooling")
    manifest {
        attributes["Main-Class"] = "uk.gov.ukho.ais.invokestepfunction.InvokeStepFunctionLambdaApplication"
        attributes["Start-Class"] = "uk.gov.ukho.ais.invokestepfunction.InvokeStepFunctionLambdaApplication"
    }
    transform(PropertiesFileTransformer::class.java) {
        paths = listOf("META-INF/spring.factories")
        mergeStrategy = "append"
    }
}

dependencies {
    implementation(project(":lambdas:libraries:heatmap-job-model"))
    compile("org.springframework.cloud:spring-cloud-function-context")
    compileOnly("com.amazonaws:aws-lambda-java-core:${Versions.lambdaJavaCore}")
    implementation("org.springframework.cloud:spring-cloud-function-adapter-aws:${Versions.springCloudFunction}")
    implementation("com.fasterxml.jackson.core:jackson-databind:${Versions.jacksonDatabind}")
    implementation(platform("com.amazonaws:aws-java-sdk-bom:${Versions.mavenAwsBom}"))
    implementation("com.amazonaws:aws-lambda-java-events:${Versions.lambdaJavaEvents}")
    implementation("com.amazonaws:aws-java-sdk-stepfunctions")
    implementation("com.amazonaws:aws-java-sdk-s3")
    implementation("com.oath.cyclops:cyclops:${Versions.cyclops}")
    implementation("com.oath.cyclops:cyclops-jackson-integration:${Versions.cyclops}")
}
