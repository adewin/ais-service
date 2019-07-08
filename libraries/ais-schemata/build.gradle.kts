plugins {
    scala
}

dependencies {
    val scalaLibrary = "org.scala-lang:scala-library:${Versions.scala}"
    val sparkSqlDependency = "org.apache.spark:spark-sql_${Versions.scalaCompat}:${Versions.spark}"

    implementation(scalaLibrary)
    compile(scalaLibrary) // required for wartremover 0.9.6 to detect the Versions of scala

    constraints {
        implementation("io.spray:spray-json_${Versions.scalaCompat}:${Versions.sprayJson}") {
            because("Version brought in by Scala version 2.11.2 has vulnerabilities")
        }
        implementation("com.google.guava:guava:${Versions.guava}") {
            because("Version brought in by Scala version 2.11.2 has vulnerabilities")
        }
    }

    compileOnly(sparkSqlDependency)
}
