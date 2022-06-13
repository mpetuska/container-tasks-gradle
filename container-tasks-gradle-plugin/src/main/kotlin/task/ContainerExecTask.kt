package dev.petuska.container.task

import dev.petuska.container.task.runner.ContainerRunner
import dev.petuska.container.util.PrefixedLogger
import org.gradle.api.provider.Provider
import org.gradle.process.internal.ExecHandle
import org.gradle.process.internal.ExecHandleFactory
import java.io.File
import java.lang.ProcessBuilder.Redirect
import javax.inject.Inject

@Suppress("LeakingThis", "TooManyFunctions")
public abstract class ContainerExecTask(
  logMarker: String = "container-exec",
) : AsyncExecTask<ExecHandle, ContainerRunner>(
  handleType = ContainerRunner.Handle::class,
  logMarker = logMarker
), ContainerExecInputs {

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

  protected open fun prepareCommandArgs(mode: Mode): List<String> = args.get()

  protected open fun prepareContainerArgs(mode: Mode): List<String> {
    val args = mutableListOf(
      "-i",
      "--rm",
      "--init",
      "--network=host",
      "-w=${workingDir.asFile.get().absolutePath}",
    )
    environment.get().forEach { (k, v) ->
      args += "-e=$k=$v"
    }
    args += containerVolumes.get().mapNotNull { (k, v) -> buildContainerVolume(k, v) }
    return args + containerArgs.get()
  }

  protected open fun prepareContainerExecutable(mode: Mode, executable: String): String = executable

  override fun buildRunner(): ContainerRunner = ContainerRunner(
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
