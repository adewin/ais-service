plugins {
    java
}

sourceSets {
    register("systemTest") {
        java
        resources
        compileClasspath += configurations.testRuntime
        runtimeClasspath += output + compileClasspath
    }
}

tasks {
    register<Test>("systemTest") {
        description = "Runs the system tests."
        group = "verification"
        testClassesDirs = sourceSets["systemTest"].output.classesDirs
        classpath = sourceSets["systemTest"].runtimeClasspath
    }
}

dependencies {
    configurations["systemTestImplementation"]("junit:junit:${Versions.junit}")
    configurations["systemTestImplementation"]("org.apache.commons:commons-io:1.3.2")
    configurations["systemTestImplementation"]("com.amazonaws:aws-java-sdk-core:1.11.610")
    configurations["systemTestImplementation"]("com.amazonaws:aws-java-sdk-athena:1.11.610")
    configurations["systemTestImplementation"]("com.amazonaws:aws-java-sdk-s3:1.11.610")
    configurations["systemTestImplementation"]("org.locationtech.geotrellis:geotrellis-s3_${Versions.scalaCompat}:${Versions.geotrellis}")
    configurations["systemTestImplementation"]("org.locationtech.geotrellis:geotrellis-spark_${Versions.scalaCompat}:${Versions.geotrellis}")
    configurations["systemTestImplementation"]("org.assertj:assertj-core:${Versions.assertJ}")
}
