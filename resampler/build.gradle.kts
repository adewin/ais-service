import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    scala
    application
    id("com.google.cloud.tools.jib") version "1.5.1"
}

dependencies {
    implementation("org.scala-lang:scala-library:${Versions.scala}")
    compile("org.scala-lang:scala-library:${Versions.scala}")
    implementation("org.slf4j:slf4j-simple:${Versions.sl4j}")
    implementation(rootProject.files("libs/${Versions.athenaJdbcLib}"))
    implementation("com.amazonaws:aws-java-sdk-core:1.11.610")
    implementation("com.github.scopt:scopt_${Versions.scalaCompat}:${Versions.scopt}")
    implementation("org.locationtech.geotrellis:geotrellis-spark_${Versions.scalaCompat}:${Versions.geotrellis}")
    implementation("org.locationtech.geotrellis:geotrellis-s3_${Versions.scalaCompat}:${Versions.geotrellis}")
    implementation("software.amazon.awssdk:s3:2.7.26")
    implementation("org.apache.commons:commons-io:1.3.2")
    implementation("org.apache.commons:commons-compress:${Versions.commonsCompress}")
    testImplementation("junit:junit:${Versions.junit}")
    testImplementation("org.assertj:assertj-core:${Versions.assertJ}")
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
    mainClassName = "uk.gov.ukho.ais.resampler.Main"
    applicationDefaultJvmArgs = listOf("-Dspark.master=local[*]")
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
