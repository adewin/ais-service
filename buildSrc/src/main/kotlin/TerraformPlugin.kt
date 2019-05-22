import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.process.ExecSpec
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File

const val terraformVersion = "0.11.14"
const val terraformBinaryUrl = "https://releases.hashicorp.com/terraform/$terraformVersion/terraform_${terraformVersion}_linux_amd64.zip"

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