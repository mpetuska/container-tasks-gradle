package dev.petuska.container.task.runner

import dev.petuska.container.task.ContainerExecInputs
import dev.petuska.container.task.ContainerExecTask.Mode
import dev.petuska.container.util.PrefixedLogger
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.deployment.internal.Deployment
import org.gradle.deployment.internal.DeploymentHandle
import org.gradle.internal.service.ServiceRegistry
import org.gradle.process.ExecResult
import org.gradle.process.ExecSpec
import org.gradle.process.internal.ExecActionFactory
import org.gradle.process.internal.ExecHandle
import org.gradle.process.internal.ExecHandleFactory
import java.io.File
import javax.inject.Inject

internal data class ContainerRunner(
  val execHandleFactory: ExecHandleFactory,
  override val workingDir: DirectoryProperty,
  override val environment: MapProperty<String, Any>,
  override val executable: Property<String>,
  override val args: ListProperty<String>,
  override val mode: Property<Mode>,
  override val image: Property<String>,
  override val version: Property<String>,
  override val containerVolumes: MapProperty<File, File>,
  override val containerArgs: ListProperty<String>,
  override val ignoreExitValue: Property<Boolean>,
  val prepareContainerArgs: (Mode) -> List<String> = { args.get() },
  val prepareCommandArgs: (Mode) -> List<String> = { containerArgs.get() },
  val prepareContainerExecutable: (Mode, String) -> String = {_,_ -> executable.get() },
  val logger: PrefixedLogger? = null
) : ContainerExecInputs {
  override fun getExtensions(): ExtensionContainer = error("Not implemented")

  override fun getName(): String = executable.get()

  fun execute(services: ServiceRegistry): ExecResult =
    services.get(ExecActionFactory::class.java).newExecAction().also(::configureExec).execute()

  fun start(): ExecHandle {
    val execHandle = execHandleFactory.newExec().also(::configureExec).build()
    execHandle.start()
    return execHandle
  }

  private fun configureExec(exec: ExecSpec) {
    exec.isIgnoreExitValue = ignoreExitValue.getOrElse(false)
    exec.workingDir(workingDir)
    exec.environment(environment.get())

    val cmdArgs = prepareCommandArgs(mode.get())
    if (mode.get() == Mode.NATIVE) {
      exec.executable(executable.get())
      exec.args(cmdArgs)
    } else {
      exec.executable(mode.get().executable)
      val cmd = listOf(
        "run"
      ) + prepareContainerArgs(mode.get()) + listOf(
        "${image.get()}:${version.get()}",
        prepareContainerExecutable(mode.get(), executable.get()),
      )
      exec.args(cmd + cmdArgs)
    }
    logger?.info { "Executing[${exec.executable}]: ${exec.executable} ${exec.args.joinToString(" ")}" }
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
