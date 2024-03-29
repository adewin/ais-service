plugins {
    java
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:${Versions.jacksonDatabind}")
    implementation(platform("com.amazonaws:aws-java-sdk-bom:${Versions.mavenAwsBom}"))
    implementation("com.amazonaws:aws-lambda-java-core:${Versions.lambdaJavaCore}")
    implementation("com.amazonaws:aws-lambda-java-events:${Versions.lambdaJavaCore}")
    implementation("com.amazonaws:aws-java-sdk-emr")
    implementation(project(":lambdas:libraries:emr-job-runner"))
    implementation(project(":lambdas:libraries:s3-event-handling"))

    testImplementation("junit:junit:${Versions.junit}")
    testImplementation("org.assertj:assertj-core:${Versions.assertJ}")
    testImplementation("org.mockito:mockito-core:${Versions.mockito}")
    testImplementation("com.github.stefanbirkner:system-rules:${Versions.systemRules}")
    testImplementation(project(":lambdas:libraries:s3-test-util"))
}