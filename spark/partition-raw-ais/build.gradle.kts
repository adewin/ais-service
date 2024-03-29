import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    scala
    application
}

dependencies {
    val scalaLibrary = "org.scala-lang:scala-library:${Versions.scala}"
    val sparkCoreDependency = "org.apache.spark:spark-core_${Versions.scalaCompat}:${Versions.spark}"
    val sparkSqlDependency = "org.apache.spark:spark-sql_${Versions.scalaCompat}:${Versions.spark}"

    implementation(scalaLibrary)
    compile(scalaLibrary) // required for wartremover 0.9.6 to detect the Versions of scala

    implementation("org.apache.hadoop:hadoop-aws:${Versions.hadoop}")
    implementation("org.apache.hadoop:hadoop-common:${Versions.hadoop}")
    implementation("com.github.scopt:scopt_${Versions.scalaCompat}:${Versions.scopt}")

    implementation(project(":spark:libraries:spark-job"))
    implementation(project(":spark:libraries:ais-schemata"))

    implementation(rootProject.files("libs/${Versions.athenaJdbcLib}"))

    constraints {
        implementation("io.spray:spray-json_${Versions.scalaCompat}:${Versions.sprayJson}") {
            because("Version brought in by Scala version 2.11.2 has vulnerabilities")
        }
        implementation("com.google.guava:guava:${Versions.guava}") {
            because("Version brought in by Scala version 2.11.2 has vulnerabilities")
        }
    }

    compileOnly(sparkCoreDependency)
    compileOnly(sparkSqlDependency)

    testImplementation(sparkCoreDependency)
    testImplementation(sparkSqlDependency)
    testImplementation("junit:junit:${Versions.junit}")
    testImplementation("org.assertj:assertj-core:${Versions.assertJ}")
    testImplementation(project(":spark:libraries:test-support"))
}

application {
    mainClassName = "uk.gov.ukho.ais.partitioning.PartitionRawAis"
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
