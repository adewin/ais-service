plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

gradlePlugin {
    plugins {
        register("terraform-plugin") {
            id = "terraform"
            implementationClass = "TerraformPlugin"
        }
    }
}
