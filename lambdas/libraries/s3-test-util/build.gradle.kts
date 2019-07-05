plugins {
    java
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:${Versions.jacksonDatabind}")
    implementation(platform("com.amazonaws:aws-java-sdk-bom:${Versions.mavenAwsBom}"))
    implementation("com.amazonaws:aws-lambda-java-events:${Versions.lambdaJavaCore}")
}