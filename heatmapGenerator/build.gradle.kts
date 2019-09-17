import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    scala
    application
    id("com.google.cloud.tools.jib")
}

dependencies {
    implementation("org.scala-lang:scala-library:${Versions.scala}")
    compile("org.scala-lang:scala-library:${Versions.scala}")
    implementation(rootProject.files("libs/${Versions.athenaJdbcLib}"))
    implementation("com.amazonaws:aws-java-sdk-core:${Versions.awsJavaSdkCore}")
    implementation("com.github.scopt:scopt_${Versions.scalaCompat}:${Versions.scopt}")
    implementation("org.locationtech.geotrellis:geotrellis-spark_${Versions.scalaCompat}:${Versions.geotrellis}")
    implementation("org.locationtech.geotrellis:geotrellis-s3_${Versions.scalaCompat}:${Versions.geotrellis}")
    implementation("software.amazon.awssdk:s3:${Versions.awsSdk}")
    implementation("org.apache.commons:commons-io:${Versions.commonsIo}")
    implementation("org.apache.commons:commons-compress:${Versions.commonsCompress}")
    testImplementation("junit:junit:${Versions.junit}")
    testImplementation("org.assertj:assertj-core:${Versions.assertJ}")
    testImplementation("org.apache.commons:commons-math3:${Versions.commonsMath3}")
    testImplementation("org.mockito:mockito-scala_${Versions.scalaCompat}:${Versions.mockitoScala}")

    constraints {
        implementation("io.spray:spray-json_${Versions.scalaCompat}:${Versions.sprayJson}") {
            because("Version brought in by Scala version 2.11.2 has vulnerabilities")
        }
        implementation("com.google.guava:guava:${Versions.guava}") {
            because("Version brought in by Scala version 2.11.2 has vulnerabilities")
        }
    }
}

application {
    mainClassName = "uk.gov.ukho.ais.heatmaps.generator.Main"
}

jib {
    to {
        image = "${System.getenv("DOCKER_REGISTRY_URL")}/ais-generate-heatmaps"
        tags = setOf("latest", project.version.toString())
        auth {
            username = System.getenv("DOCKER_REGISTRY_USERNAME")
            password = System.getenv("DOCKER_REGISTRY_PASSWORD")
        }
    }
}

tasks.withType<ShadowJar> {
    isZip64 = true

    manifest {
        attributes["Main-Class"] = application.mainClassName
    }
}
