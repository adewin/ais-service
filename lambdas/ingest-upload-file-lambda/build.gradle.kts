plugins {
    java
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:${Versions.jacksonDatabind}")
    implementation(platform("com.amazonaws:aws-java-sdk-bom:${Versions.mavenAwsBom}"))
    implementation("com.amazonaws:aws-lambda-java-core:${Versions.lambdaJavaCore}")
    implementation("com.amazonaws:aws-lambda-java-events:${Versions.lambdaJavaCore}")
    implementation("com.amazonaws:aws-java-sdk-s3")
    implementation("org.apache.logging.log4j:log4j-core:${Versions.log4j}")
    implementation(project(":lambdas:libraries:s3-event-handling"))

    testImplementation("junit:junit:${Versions.junit}")
    testImplementation("org.assertj:assertj-core:${Versions.assertJ}")
    testImplementation("org.mockito:mockito-core:${Versions.mockito}")
    testImplementation(project(":lambdas:libraries:s3-test-util"))
}
