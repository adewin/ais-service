plugins {
    scala
    application
}

application {
    mainClassName = "uk.gov.ukho.ais.heatmaps.aggregate.Main"
}

dependencies {
    implementation("org.scala-lang:scala-library:${Versions.scala}")
    compile("org.scala-lang:scala-library:${Versions.scala}")
    implementation("software.amazon.awssdk:s3:2.7.26")
    implementation("org.apache.commons:commons-io:1.3.2")
    implementation("com.github.scopt:scopt_${Versions.scalaCompat}:${Versions.scopt}")
    implementation("org.locationtech.geotrellis:geotrellis-spark_${Versions.scalaCompat}:${Versions.geotrellis}")
    implementation("org.locationtech.geotrellis:geotrellis-s3_${Versions.scalaCompat}:${Versions.geotrellis}")

    constraints {
        implementation("io.spray:spray-json_${Versions.scalaCompat}:${Versions.sprayJson}") {
            because("Version brought in by Scala version 2.11.2 has vulnerabilities")
        }
        implementation("com.google.guava:guava:${Versions.guava}") {
            because("Version brought in by Scala version 2.11.2 has vulnerabilities")
        }
    }
}