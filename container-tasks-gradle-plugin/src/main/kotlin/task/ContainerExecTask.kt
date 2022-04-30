package dev.petuska.container.task

import dev.petuska.container.task.runner.ContainerRunner
import dev.petuska.container.task.runner.ContainerRunner.Handle
import dev.petuska.container.util.PrefixedLogger
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.deployment.internal.DeploymentRegistry
import org.gradle.process.internal.ExecHandleFactory
import java.io.File
import java.lang.ProcessBuilder.Redirect
import javax.inject.Inject

@Suppress("LeakingThis", "TooManyFunctions")
public abstract class ContainerExecTask(
  logMarker: String = "container-exec",
) : DefaultTask(), ContainerExecInputs {
  private val _logger: PrefixedLogger = PrefixedLogger(logMarker, super.getLogger())

  @Internal
  public override fun getLogger(): PrefixedLogger = _logger

  @get:Inject
  protected abstract val execHandleFactory: ExecHandleFactory

  init {
    workingDir.convention(project.layout.dir(project.provider { temporaryDir }))
    mode.convention(project.provider { Mode.detect(executable.get()) })
    version.convention("latest")

    addContainerVolume(workingDir.asFile)
    addContainerVolume(project.provider { project.rootDir.resolve(".git") })
  }

  protected fun addContainerVolume(hostFile: Provider<File>, containerFile: Provider<File> = hostFile) {
    containerVolumes.putAll(
      project.provider {
        if (hostFile.isPresent && containerFile.isPresent) mapOf(hostFile.get() to containerFile.get()) else mapOf()
      }
    )
  }

  private fun buildContainerVolume(hostFile: File, containerFile: File = hostFile): String? = if (hostFile.exists()) {
    "-v=${hostFile.absolutePath}:${containerFile.absolutePath}"
      .let { if (mode.get() == DOCKER) it else "$it:Z" }
  } else null

  protected fun <T> Property<T>.setFinal(value: T) {
    set(value)
    finalizeValue()
  }

  protected open fun beforeAction(): Unit = Unit

  protected open fun afterAction(): Unit = Unit

  protected open fun prepareCommandArgs(mode: Mode): List<String> {
    return args.get()
  }

  protected open fun prepareContainerArgs(mode: Mode): List<String> {
    val args = mutableListOf(
      "-i",
      "--rm",
      "--init",
      "--network=host",
      "-w=${workingDir.asFile.get().absolutePath}",
    )
    if (mode == PODMAN) args += "-e=JEKYLL_ROOTLESS=1"
    environment.get().forEach { (k, v) ->
      args += "-e=$k=$v"
    }
    args += containerVolumes.get().mapNotNull { (k, v) -> buildContainerVolume(k, v) }
    return args + containerArgs.get()
  }

  protected open fun prepareContainerExecutable(mode: Mode, executable: String): String = executable

  private fun execute() {
    val isContinuous = project.gradle.startParameter.isContinuous
    val runner = ContainerRunner(
      execHandleFactory = execHandleFactory,
      executable = executable,
      args = args,
      containerArgs = containerArgs,
      workingDir = workingDir,
      ignoreExitValue = ignoreExitValue,
      environment = environment,
      mode = mode,
      version = version,
      image = image,
      containerVolumes = containerVolumes,
      prepareCommandArgs = ::prepareCommandArgs,
      prepareContainerArgs = ::prepareContainerArgs,
      prepareContainerExecutable = ::prepareContainerExecutable,
      logger = logger
    )
    if (isContinuous) {
      val deploymentRegistry = services.get(DeploymentRegistry::class.java)
      val deploymentHandle = deploymentRegistry.get("jekyll", Handle::class.java)
      if (deploymentHandle == null) {
        deploymentRegistry.start(
          "jekyll",
          DeploymentRegistry.ChangeBehavior.BLOCK,
          Handle::class.java, runner
        )
      }
    } else {
      runner.execute(services).assertNormalExitValue()
    }
  }

  @TaskAction
  @Suppress("UnusedPrivateMember")
  private fun action() {
    beforeAction()
    execute()
    afterAction()
  }

  public enum class Mode {
    NATIVE, PODMAN, DOCKER;

    public val executable: String = name.lowercase()

    public companion object {
      private fun hasExecutable(executable: String): Boolean {
        val pb = ProcessBuilder("which", executable)
          .redirectOutput(Redirect.DISCARD)
          .redirectError(Redirect.DISCARD)
        return runCatching { pb.start().also(Process::waitFor).exitValue() }.getOrDefault(1) == 0
      }

      public fun detectOrNull(executable: String, logger: PrefixedLogger? = null): Mode? {
        return if (hasExecutable(executable)) {
          logger?.info { "Native $executable detected" }
          NATIVE
        } else if (hasExecutable(PODMAN.executable)) {
          logger?.info { "Podman detected. Using it to run $executable via a rootless container" }
          PODMAN
        } else if (hasExecutable(DOCKER.executable)) {
          logger?.info { "Docker detected. Using it to run $executable via a root container" }
          DOCKER
        } else null
      }

      public fun detect(executable: String, logger: PrefixedLogger? = null): Mode =
        detectOrNull(executable, logger) ?: error("No $executable, podman or docker executable found")
    }
  }
}
