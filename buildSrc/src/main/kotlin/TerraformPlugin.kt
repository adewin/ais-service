import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.process.ExecSpec
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File

const val terraformVersion = "0.12.0"
val terraformBinaryUrl = "https://releases.hashicorp.com/terraform/$terraformVersion/terraform_${terraformVersion}_${getOsName()}_amd64.zip"

fun getOsName(): String = if (System.getProperty("os.name").toLowerCase().contains("windows")) {
    "windows"
} else {
    "linux"
}

val Project.terraformBinary
    get() = File(buildDir, "bin/terraform")
val Project.terraformWorkingDir
    get() = File(projectDir, "src/main/terraform")

class TerraformPlugin : Plugin<Project> {
    override fun apply(project: Project): Unit = project.run {
        task("downloadTerraform") {
            outputs.file(terraformBinary)
            outputs.upToDateWhen { terraformBinary.exists() }
            doLast {
                val archive = "$terraformBinary.zip"
                exec { commandLine("wget", "-q", terraformBinaryUrl, "-O", archive) }
                exec {
                    workingDir = terraformBinary.parentFile
                    commandLine("unzip", "-qq", "-o", archive)
                }
                exec { commandLine("rm", archive) }
            }
        }
    }
}

fun ExecSpec.stdin(value: String) {
    this.standardInput = ByteArrayInputStream(value.toByteArray())
}

fun Project.execTerraform(specCustomiser: ExecSpec.() -> Unit): String {
    val standardOutputStream = ByteArrayOutputStream()
    exec {
        standardOutput = standardOutputStream
        workingDir = terraformWorkingDir
        executable = terraformBinary.absolutePath
        specCustomiser(this)
    }
    return standardOutputStream.toString()
}
