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
        attributes["Main-Class"] = "uk.gov.ukho.ais.lambda.handleheatmapoutcome.HandleHeatmapOutcomeLambdaApplication"
        attributes["Start-Class"] = "uk.gov.ukho.ais.lambda.handleheatmapoutcome.HandleHeatmapOutcomeLambdaApplication"
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
    implementation("com.amazonaws:aws-lambda-java-events:${Versions.lambdaJavaCore}")
    implementation("com.amazonaws:aws-java-sdk-s3")
    implementation("com.oath.cyclops:cyclops:${Versions.cyclops}")
    implementation("com.oath.cyclops:cyclops-jackson-integration:${Versions.cyclops}")
    implementation("org.apache.commons:commons-lang3:${Versions.commonsLang}")
    implementation("org.apache.commons:commons-io:${Versions.commonsIO}")

    testImplementation("junit:junit:${Versions.junit}")
    testImplementation("org.assertj:assertj-core:${Versions.assertJ}")
    testImplementation("org.mockito:mockito-core:${Versions.mockito}")
    testImplementation("org.springframework.boot:spring-boot-starter-test:${Versions.springBoot}")
}
