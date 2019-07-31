import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.internal.io.NullOutputStream
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import org.gradle.process.ExecSpec
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject

internal const val terraformVersion = "0.12.1"
internal const val terraformExtensionName = "terraform"
internal const val terraformTaskClean = "terraformClean"
internal const val terraformTaskDownload = "terraformDownload"
internal const val terraformTaskInit = "terraformInit"
internal const val terraformTaskValidate = "terraformValidate"
internal const val terraformTaskPlan = "terraformPlan"
internal const val terraformTaskApply = "terraformApply"

internal val osName = with(System.getProperty("os.name").toLowerCase()) {
    when {
        contains("windows") -> "windows"
        contains("mac os") -> "darwin"
        else -> "linux"
    }
}

internal val terraformBinaryUrl = "https://releases.hashicorp.com/terraform/$terraformVersion/terraform_${terraformVersion}_${osName}_amd64.zip"
internal val Project.terraformBinary
    get() = File(buildDir, "bin/terraform")
internal val Project.terraformWorkingDir
    get() = File(projectDir, "src/main/terraform")
internal val Project.terraformInitDir
    get() = File(terraformWorkingDir, ".terraform")

class TerraformPlugin : Plugin<Project> {
    override fun apply(project: Project): Unit = project.run {
        val terraformExtension = extensions.create<TerraformExtension>(terraformExtensionName)

        tasks.register<Delete>(terraformTaskClean) {
            delete(terraformBinary.absolutePath)
            delete(terraformInitDir.absolutePath)
        }
        tasks.getByName("clean").dependsOn(terraformTaskClean)

        afterEvaluate {
            tasks.register(terraformTaskDownload) {
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

            tasks.register<TerraformInitTask>(terraformTaskInit) {
                outputs.dir(terraformInitDir)
                outputs.upToDateWhen { terraformInitDir.exists() }
                args("init")
            }

            tasks.register<TerraformTask>(terraformTaskValidate) {
                args("validate")
                configureUsing(terraformExtension)
                mustRunAfter("spotlessTerraform")
            }
            tasks.getByName("check").dependsOn(terraformTaskValidate)

            tasks.register<TerraformTask>(terraformTaskPlan) {
                args("plan")
                configureUsing(terraformExtension)
            }

            tasks.register<TerraformApplyTask>(terraformTaskApply) {
                args("apply")
                configureUsing(terraformExtension)
            }
        }
    }
}

open class TerraformExtension {
    val dependencies = mutableSetOf<Task>()
    val environmentVariables = mutableMapOf<String, String>()

    fun dependsOn(vararg newDependencies: Task) {
        this.dependencies += newDependencies
    }

    fun environmentVariables(vararg newEnvironmentVariables: Pair<String, String>) {
        this.environmentVariables += newEnvironmentVariables
    }
}

open class TerraformInitTask @Inject constructor() : Exec() {
    init {
        this.dependsOn(terraformTaskDownload)
        standardOutput = NullOutputStream.INSTANCE
        workingDir = project.terraformWorkingDir
        executable = project.terraformBinary.absolutePath
    }

    @Option(option = "interactive", description = "Connect task to stdout and stdin. Overrides task configuration.")
    @get:Input
    var interactive: Boolean = false

    @TaskAction
    override fun exec() {
        if (interactive) {
            standardOutput = System.out
            standardInput = System.`in`
        }
        super.exec()
    }
}

open class TerraformTask @Inject constructor() : TerraformInitTask() {
    init {
        this.dependsOn(terraformTaskInit)
    }

    fun configureUsing(terraformExtension: TerraformExtension) {
        dependsOn(terraformExtension.dependencies)
        environment(terraformExtension.environmentVariables)
    }
}

open class TerraformApplyTask @Inject constructor() : TerraformTask() {
    init {
        this.dependsOn(terraformTaskInit)
    }

    @Option(option = "auto-approve", description = "Skip interactive approval of plan before applying. Overrides task configuration.")
    @get:Input
    var autoApprove: Boolean = false

    @TaskAction
    override fun exec() {
        if (autoApprove) {
            args("-auto-approve")
        }
        super.exec()
    }
}

fun ExecSpec.stdin(value: String) {
    this.standardInput = ByteArrayInputStream(value.toByteArray())
}

fun Project.terraformExec(configureExec: ExecSpec.() -> Unit): String {
    val standardOutputStream = ByteArrayOutputStream()
    exec {
        standardOutput = standardOutputStream
        workingDir = terraformWorkingDir
        executable = terraformBinary.absolutePath
        configureExec()
    }
    return standardOutputStream.toString()
}
