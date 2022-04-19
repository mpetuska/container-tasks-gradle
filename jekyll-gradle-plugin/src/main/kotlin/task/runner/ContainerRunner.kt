package dev.petuska.jekyll.task.worker

import dev.petuska.jekyll.extension.domain.JekyllMode
import dev.petuska.jekyll.extension.domain.JekyllMode.NATIVE
import dev.petuska.jekyll.util.PluginLogger
import org.gradle.api.file.Directory
import org.gradle.deployment.internal.Deployment
import org.gradle.deployment.internal.DeploymentHandle
import org.gradle.internal.service.ServiceRegistry
import org.gradle.process.ExecResult
import org.gradle.process.ExecSpec
import org.gradle.process.internal.ExecActionFactory
import org.gradle.process.internal.ExecHandle
import org.gradle.process.internal.ExecHandleFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject

internal data class ContainerRunner(
  val execHandleFactory: ExecHandleFactory,
  val executable: String,
  val commandArgs: List<String>,
  val containerArgs: List<String>,
  val workingDir: Directory,
  val ignoreExitValue: Boolean,
  val environment: Map<String, Any>,
  val mode: JekyllMode,
  val version: String,
) : PluginLogger {
  override fun getLogger(): Logger = LoggerFactory.getLogger(ContainerRunner::class.java)

  fun execute(services: ServiceRegistry): ExecResult =
    services.get(ExecActionFactory::class.java).newExecAction().also(::configureExec).execute()

  fun start(): ExecHandle {
    val execHandle = execHandleFactory.newExec().also(::configureExec).build()
    execHandle.start()
    return execHandle
  }

  private fun configureExec(exec: ExecSpec) {
    exec.isIgnoreExitValue = ignoreExitValue
    exec.workingDir(workingDir)
    exec.environment(environment)

    if (mode == NATIVE) {
      exec.executable(executable)
      exec.args(commandArgs)
    } else {
      exec.executable(mode.name.lowercase())
      val cmd = listOf(
        "run"
      ) + containerArgs + listOf(
        "docker.io/jekyll/jekyll:$version",
        executable,
      )
      exec.args(cmd + commandArgs)
    }
    info { "Executing[${exec.executable}]: ${exec.executable} ${exec.args.joinToString(" ")}" }
  }

  internal abstract class Handle @Inject constructor(private val runner: ContainerRunner) : DeploymentHandle {
    var process: ExecHandle? = null

    override fun isRunning() = process != null

    override fun start(deployment: Deployment) {
      process = runner.start()
    }

    override fun stop() {
      process?.abort()
    }
  }
}
