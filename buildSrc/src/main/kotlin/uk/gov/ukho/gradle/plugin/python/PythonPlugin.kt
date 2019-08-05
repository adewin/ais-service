package uk.gov.ukho.gradle.plugin.python

import com.diffplug.gradle.spotless.SpotlessExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.bundling.Zip
import org.gradle.internal.io.NullOutputStream
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject

internal object PythonTask {
    const val clean = "pythonClean"

    const val virtualEnvInstall = "pythonVirtualEnvInstall"
    const val virtualEnvSetup = "pythonVirtualEnvSetup"

    const val dependenciesInstall = "pythonDependenciesInstall"

    const val lintInstall = "pythonLintInstall"
    const val lint = "pythonLint"

    const val installToSite = "pythonInstallToSite"
    const val generateSetupPy = "pythonGenerateSetupPy"

    const val formatInstall = "pythonFormatInstall"
    const val testInstall = "pythonTestInstall"
    const val test = "pythonTest"

    const val distDependencies = "pythonDistDependencies"
    const val distZip = "pythonDistZip"
}

internal const val pythonSystemBinary = "python3"

internal val Project.pythonBuildDir
    get() = File(buildDir, "python")

internal val Project.pythonVirtualEnvDir
    get() = File(pythonBuildDir, "virtualenv")

internal val Project.pythonVirtualEnvBinary
    get() = File(pythonVirtualEnvDir, "bin/python")

internal val Project.pythonDistDir
    get() = File(pythonBuildDir, "dist")

internal const val pythonSrcMainDir = "src/main/python"
internal const val pythonSrcTestDir = "src/test/python"

class PythonPlugin : Plugin<Project> {
    override fun apply(project: Project): Unit = project.run {
        tasks.register<Delete>(PythonTask.clean) {
            delete(pythonBuildDir)
        }
        tasks.getByName("clean").dependsOn(PythonTask.clean)

        tasks.register<SystemPythonTask>(PythonTask.virtualEnvInstall) {
            args("pip", "install", "--user", "virtualenv")
        }

        tasks.register<SystemPythonTask>(PythonTask.virtualEnvSetup) {
            dependsOn(PythonTask.virtualEnvInstall)
            args("virtualenv", pythonVirtualEnvDir)
            doLast {
                exec {
                    commandLine("chmod", "-R", "+rwx", pythonVirtualEnvDir)
                }
            }
        }

        tasks.register<VirtualEnvPythonTask>(PythonTask.dependenciesInstall) {
            args("pip", "install", "-r", File(projectDir, "requirements.txt"))
        }

        tasks.register<VirtualEnvPythonTask>(PythonTask.lintInstall) {
            args("pip", "install", "flake8", "flake8-bugbear", "flake8-mypy", "pycodestyle", "mccabe", "pyflakes")
        }

        tasks.register<VirtualEnvPythonTask>(PythonTask.lint) {
            dependsOn(PythonTask.lintInstall)
            args("flake8", pythonSrcMainDir, pythonSrcTestDir)
        }
        tasks.getByName("check").dependsOn(PythonTask.lint)

        tasks.register<VirtualEnvPythonTask>(PythonTask.formatInstall) {
            args("pip", "install", "black")
        }

        apply { plugin("com.diffplug.gradle.spotless") }
        configure<SpotlessExtension> {
            format("python") {
                target("$pythonSrcMainDir/**/*.py", "$pythonSrcTestDir/**/*.py")
                custom("python") { fileContents ->
                    val standardOutputStream = ByteArrayOutputStream()
                    exec {
                        standardInput = fileContents.toByteArray().inputStream()
                        standardOutput = standardOutputStream
                        errorOutput = NullOutputStream.INSTANCE
                        executable = pythonVirtualEnvBinary.absolutePath
                        args = listOf("-m", "black", "-")
                    }
                    standardOutputStream.toString()
                }
            }
        }

        afterEvaluate {
            tasks.getByName("spotlessPython").dependsOn(PythonTask.formatInstall)
        }

        /*
         * Install to site
         */
        tasks.register(PythonTask.generateSetupPy) {
            File(projectDir, "$pythonSrcMainDir/setup.py").writeText("""
                from distutils.core import setup
                
                setup(name="${project.name}", version="${project.version}")
                
            """.trimIndent())
        }

        tasks.register<VirtualEnvPythonTask>(PythonTask.installToSite) {
            dependsOn(PythonTask.generateSetupPy)
            args("pip", "install", "-e", pythonSrcMainDir)
        }

        /*
         *  Pytest
         */
        tasks.register<VirtualEnvPythonTask>(PythonTask.testInstall) {
            dependsOn(PythonTask.dependenciesInstall)
            args("pip", "install", "pytest")
        }

        tasks.register<VirtualEnvPythonTask>(PythonTask.test) {
            dependsOn(PythonTask.testInstall, PythonTask.installToSite)
            args("pytest", pythonSrcTestDir)
        }
        tasks.getByName("check").dependsOn(PythonTask.test)
        tasks.register("test") {
            dependsOn(PythonTask.test)
        }

        /*
         *  Distribution
         *  Lambdas require their dependencies be included as part of the zip deployment file.
         */
        tasks.register<VirtualEnvPythonTask>(PythonTask.distDependencies) {
            args("pip", "install", "-Ur", File(projectDir, "requirements.txt"),
                    "--target", mkdir(pythonDistDir))
        }

        tasks.register<Zip>(PythonTask.distZip) {
            dependsOn(PythonTask.distDependencies)
            setExcludes(setOf("*.pyc"))
            from(pythonSrcMainDir)
            from(pythonDistDir)
        }
        tasks.getByName("build").dependsOn(PythonTask.distZip)
    }
}

open class SystemPythonTask @Inject constructor() : Exec() {
    init {
        standardOutput = System.out
        standardInput = System.`in`
        executable = pythonSystemBinary
        this.setArgs(listOf("-m"))
    }
}

open class VirtualEnvPythonTask @Inject constructor() : Exec() {
    init {
        this.dependsOn(PythonTask.virtualEnvSetup)
        standardOutput = System.out
        standardInput = System.`in`
        executable = project.pythonVirtualEnvBinary.absolutePath
        this.setArgs(listOf("-m"))
    }
}
