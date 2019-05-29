import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension

plugins {
    java
    id("io.spring.dependency-management")
    id("com.github.johnrengelman.shadow")
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "1.8"
}

tasks.withType<ShadowJar> {
    isZip64 = true
}

repositories {
    mavenCentral()
}

configure<DependencyManagementExtension> {
    imports {
        mavenBom("com.amazonaws:aws-java-sdk-bom:${Versions.mavenAwsBom}")
    }
}

dependencies {
    implementation("com.amazonaws:aws-java-sdk-emr")
    implementation("com.amazonaws:aws-lambda-java-core:${Versions.lambdaJavaCore}")
    implementation("com.amazonaws:aws-java-sdk-s3")
    testImplementation("junit:junit:${Versions.junit}")
}
