plugins {
    java
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:${Versions.jacksonDatabind}")
    implementation(platform("com.amazonaws:aws-java-sdk-bom:${Versions.mavenAwsBom}"))
    implementation("com.amazonaws:aws-java-sdk-emr")
    implementation("com.amazonaws:aws-lambda-java-core:${Versions.lambdaJavaCore}")
    implementation(project(":lambdas:libraries:emr-job-runner"))
    implementation("com.amazonaws:aws-java-sdk-s3")
    testImplementation("junit:junit:${Versions.junit}")
    testImplementation("org.assertj:assertj-core:${Versions.assertJ}")
}
