package dev.petuska.jekyll.task

import dev.petuska.jekyll.extension.domain.JekyllCommonOptions
import dev.petuska.jekyll.extension.domain.JekyllMode
import dev.petuska.jekyll.extension.domain.JekyllModeScope
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import java.io.File

@Suppress("LeakingThis")
public abstract class JekyllExecTask : DefaultTask(), JekyllModeScope, JekyllCommonOptions {
  @get:Input
  public abstract override val mode: Property<JekyllMode>

  @get:Input
  public abstract override val version: Property<String>

  @get:Input
  public abstract override val environment: MapProperty<String, Any>

  @get:Input
  @get:Optional
  public abstract val ignoreExitValue: Property<Boolean>

  @get:Input
  public abstract val args: ListProperty<String>

  @Option(option = "arg", description = "Arguments to be passed to the jekyll executable")
  public fun arg(args: List<String>) {
    this.args.addAll(args)
  }

  @get:Input
  public abstract val containerArgs: ListProperty<String>

  @get:Internal
  public abstract val workingDir: DirectoryProperty

  @get:InputFiles
  @get:Optional
  public abstract val source: ConfigurableFileCollection

  @Option(option = "source", description = "Change the directory where Jekyll will read files")
  public fun source(paths: List<String>) {
    paths.forEach(source::from)
  }

  @get:OutputDirectory
  public abstract val destination: DirectoryProperty

  @Option(option = "destination", description = "Change the directory where Jekyll will write files")
  public fun destination(path: String) {
    destination.set(File(path))
  }

  @get:Input
  @get:Option(
    option = "safe",
    description = "Disable non-whitelisted plugins, caching to disk, and ignore symbolic links."
  )
  @get:Optional
  public abstract val safe: Property<Boolean>

  @get:Internal
  protected open val command: String? = null

  init {
    group = "jekyll"
    workingDir.convention(project.layout.dir(project.provider { temporaryDir.resolve("sources") }))
    destination.convention(project.layout.dir(project.provider { temporaryDir.resolve("outputs") }))
  }

  private val containerVolumes = mutableMapOf<String, String>()

  protected fun containerVolume(hostPath: String): String? = containerVolumes[hostPath]
    ?.let { "-v=$hostPath:$it" }
    ?.let { if (mode.get() == DOCKER) it else "$it:Z" }

  protected fun containerVolume(hostFile: File): String? = containerVolume(hostFile.absolutePath)

  protected fun setContainerVolume(hostFile: File, containerFile: File): String {
    val hostPath = hostFile.absolutePath
    containerVolumes[hostPath] = containerFile.absolutePath
    return hostPath
  }

  protected fun containerPath(hostFile: File): String? =
    if (mode.get() == NATIVE) hostFile.absolutePath else containerVolumes[hostFile.absolutePath]

  @TaskAction
  private fun action() {
    beforeAction()
    if (mode.get() == NATIVE) runNative() else runContainer()
    afterAction()
  }

  protected open fun beforeAction() {
    setContainerVolume(workingDir.get().asFile, containerPwd)
    if (destination.isPresent && destination.asFile.get().absolutePath != workingDir.asFile.get().absolutePath) {
      setContainerVolume(destination.get().asFile, containerDestination)
    }
    project.copy {
      it.into(workingDir)
      it.from(source)
    }
  }

  protected open fun afterAction(): Unit = Unit
  protected open fun prepareJekyllArgs(mode: JekyllMode): List<String> {
    val args = args.get().toMutableList()
    command?.let { args.add(0, command) }
    if (safe.getOrElse(false)) args += "--safe"
    args += listOf("--source", containerPath(workingDir.get().asFile))
    if (destination.isPresent) args += listOf("--destination", containerPath(destination.get().asFile))
    return args
  }

  protected open fun prepareContainerArgs(mode: JekyllMode): List<String> {
    val args = mutableListOf(
      "-i",
      "--rm",
      "--init",
      "-e=JEKYLL_ROOTLESS=${if (mode == PODMAN) "1" else "0"}",
      "-w=$containerPwd",
    )
    args += containerVolumes.mapNotNull { (k, _) ->
      containerVolume(k)
    }
    return args + containerArgs.get()
  }

  private fun runNative() {
    val pwd = workingDir.get().asFile
    project.exec {
      it.isIgnoreExitValue = ignoreExitValue.getOrElse(false)
      it.workingDir(pwd)
      it.environment(this@JekyllExecTask.environment.get())
      it.executable(NATIVE.name.lowercase())
      val jekyllArgs = prepareJekyllArgs(mode.get())
      logger.info("Executing[${it.executable}]: jekyll ${jekyllArgs.joinToString(" ")}")
      it.args(jekyllArgs)
    }
  }

  private fun runContainer() {
    val pwd = workingDir.get().asFile
    val exe = mode.get().name.lowercase()
    project.exec {
      it.isIgnoreExitValue = ignoreExitValue.getOrElse(false)
      it.workingDir(pwd)
      it.executable(exe)
      it.environment(this@JekyllExecTask.environment.get())
      val cmd = listOf(
        "run"
      ) + prepareContainerArgs(mode.get()) + listOf(
        "docker.io/jekyll/jekyll:${version.get()}",
        "jekyll",
      )
      val jekyllArgs = prepareJekyllArgs(mode.get())
      logger.info("Executing[$exe]: $exe ${(cmd + jekyllArgs).joinToString(" ")}")
      it.args(cmd + jekyllArgs)
    }
  }

  protected inline fun File.safePath(containerPath: () -> String): String =
    if (mode.get() == NATIVE) absolutePath else containerPath()

  public companion object {
    public val containerRoot: File = File("/srv/jekyll")
    public val containerPwd: File = containerRoot.resolve("_pwd")
    public val containerSource: File = containerPwd
    public val containerDestination: File = containerRoot.resolve("_outputs")
  }
}
