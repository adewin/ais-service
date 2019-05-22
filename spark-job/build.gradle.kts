import com.diffplug.gradle.spotless.SpotlessExtension
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import cz.augi.gradle.wartremover.WartremoverExtension
import java.net.URI

plugins {
    scala
    application
    id("com.github.johnrengelman.shadow")
    id("cz.augi.gradle.wartremover")
}

repositories {
    maven { url = URI("https://repo.boundlessgeo.com/main/") }
    maven { url = URI("http://maven.geomajas.org/") }
    mavenCentral()
}

dependencies {
    val scalaLibrary = "org.scala-lang:scala-library:${Versions.scala}"
    implementation(scalaLibrary)
    compile(scalaLibrary) // required for wartremover 0.9.6 to detect the Versions of scala

    implementation("com.github.scopt:scopt_${Versions.scalaCompat}:${Versions.scopt}")

    implementation("org.locationtech.geotrellis:geotrellis-spark_${Versions.scalaCompat}:${Versions.geotrellis}")
    implementation("org.locationtech.geotrellis:geotrellis-s3_${Versions.scalaCompat}:${Versions.geotrellis}")

    implementation("org.apache.spark:spark-core_${Versions.scalaCompat}:${Versions.spark}")
    implementation("org.apache.spark:spark-sql_${Versions.scalaCompat}:${Versions.spark}")
}

application {
    mainClassName = "uk.gov.ukho.ais.rasters.AisToRaster"
    applicationDefaultJvmArgs = listOf("-Dspark.master=local[*]")
}

tasks.withType<ShadowJar> {
    isZip64 = true

    manifest {
        attributes["Main-Class"] = application.mainClassName
    }

    dependencies {
        exclude(dependency("org.apache.spark:.*"))
    }
}

configure<SpotlessExtension> {
    scala {
        scalafmt()
    }
}

configure<WartremoverExtension> {
    warningWarts = warningWarts - "NonUnitStatements"
}
