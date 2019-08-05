import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    id("org.springframework.boot")
}

tasks.getByName("build").dependsOn("shadowJar")
apply(plugin = "org.springframework.boot")

tasks.withType<ShadowJar> {
    isZip64 = true
    classifier = "aws"
    // Required for Spring
    mergeServiceFiles()
    append("META-INF/spring.handlers")
    append("META-INF/spring.schemas")
    append("META-INF/spring.tooling")
    manifest {
        attributes["Main-Class"] = "uk.gov.ukho.ais.triggerresamplelambda.TriggerResampleLambdaApplication"
        attributes["Start-Class"] = "uk.gov.ukho.ais.triggerresamplelambda.TriggerResampleLambdaApplication"
    }
}

dependencies {
    compile("org.springframework.cloud:spring-cloud-function-context")
    compileOnly("com.amazonaws:aws-lambda-java-core:${Versions.lambdaJavaCore}")
    implementation("org.springframework.cloud:spring-cloud-function-adapter-aws:${Versions.springCloudFunction}")
    implementation("com.fasterxml.jackson.core:jackson-databind:${Versions.jacksonDatabind}")
    implementation(platform("com.amazonaws:aws-java-sdk-bom:${Versions.mavenAwsBom}"))
    implementation("com.amazonaws:aws-lambda-java-events:${Versions.lambdaJavaCore}")
    implementation("com.amazonaws:aws-java-sdk-s3")
    implementation("com.amazonaws:aws-java-sdk-emr")
    implementation(project(":lambdas:libraries:emr-job-runner"))

    testImplementation("junit:junit:${Versions.junit}")
    testImplementation("org.assertj:assertj-core:${Versions.assertJ}")
    testImplementation("org.mockito:mockito-core:${Versions.mockito}")
    testImplementation("org.springframework.boot:spring-boot-starter-test:${Versions.springBoot}")
}
