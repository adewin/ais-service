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
    configurations["systemTestImplementation"]("com.amazonaws:aws-java-sdk-core:1.11.610")
    configurations["systemTestImplementation"]("com.amazonaws:aws-java-sdk-athena:1.11.610")
    configurations["systemTestImplementation"]("com.amazonaws:aws-java-sdk-s3:1.11.610")
}
