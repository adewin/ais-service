plugins {
    `kotlin-dsl`
}

repositories {
    jcenter()
}

dependencies {
    implementation("com.diffplug.spotless:spotless-plugin-gradle:3.24.0")
}

gradlePlugin {
    plugins {
        register("terraform-plugin") {
            id = "terraform"
            implementationClass = "TerraformPlugin"
        }
        register("python-plugin") {
            id = "python"
            implementationClass = "uk.gov.ukho.gradle.plugin.python.PythonPlugin"
        }
    }
}
