package dev.petuska.jekyll.task

import dev.petuska.jekyll.extension.domain.JekyllCommonOptions
import dev.petuska.jekyll.extension.domain.JekyllMode
import dev.petuska.jekyll.extension.domain.JekyllModeScope
import dev.petuska.jekyll.task.worker.ContainerRunner
import dev.petuska.jekyll.util.PluginLogger
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.deployment.internal.DeploymentRegistry
import org.gradle.process.internal.ExecHandleFactory
import java.io.File
import java.io.OutputStream
import javax.inject.Inject

@Suppress("LeakingThis", "TooManyFunctions")
public abstract class ContainerExecTask internal constructor(private val executable: String) :
  DefaultTask(),
  JekyllModeScope,
  JekyllCommonOptions,
  PluginLogger {
  @get:Input
  @get:Optional
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

  @get:Inject
  protected abstract val execHandleFactory: ExecHandleFactory

  init {
    workingDir.convention(project.layout.dir(project.provider { temporaryDir.resolve("pwd") }))
  }

  private val containerVolumes = mutableSetOf<File>()

  private fun getContainerVolume(hostFile: File, containerFile: File = hostFile): String? = if (hostFile.exists()) {
    "-v=${hostFile.absolutePath}:${containerFile.absolutePath}"
      .let { if (resolvedMode == DOCKER) it else "$it:Z" }
  } else null

  protected fun addContainerVolume(hostFile: File) {
    hostFile.mkdirs()
    containerVolumes.add(hostFile)
  }

  @TaskAction
  @Suppress("UnusedPrivateMember")
  private fun action() {
    resolveMode()
    beforeAction()
    execute()
    afterAction()
  }

  private lateinit var resolvedMode: JekyllMode

  private fun resolveMode(): JekyllMode {
    resolvedMode = mode.orElse(
      project.provider {
        fun hasExecutable(executable: String): Boolean = project.exec {
          it.isIgnoreExitValue = true
          it.commandLine("which", executable)
          it.environment(environment.get())
          it.errorOutput = OutputStream.nullOutputStream()
          it.standardOutput = OutputStream.nullOutputStream()
        }.exitValue == 0
        if (hasExecutable(executable)) {
          info { "Native $executable detected" }
          JekyllMode.NATIVE
        } else if (hasExecutable("podman")) {
          info { "Podman detected. Using it to run $executable via a rootless container" }
          JekyllMode.PODMAN
        } else if (hasExecutable("docker")) {
          info { "Docker detected. Using it to run $executable via a root container" }
          JekyllMode.DOCKER
        } else {
          error("No $executable, podman or docker executable found")
        }
      }
    ).get()
    return resolvedMode
  }

  private fun execute() {
    val isContinuous = project.gradle.startParameter.isContinuous
    val runner = ContainerRunner(
      execHandleFactory = execHandleFactory,
      executable = executable,
      commandArgs = prepareCommandArgs(resolvedMode),
      containerArgs = prepareContainerArgs(resolvedMode),
      workingDir = workingDir.get(),
      ignoreExitValue = ignoreExitValue.getOrElse(isContinuous),
      environment = System.getenv() + environment.get(),
      mode = resolvedMode,
      version = version.get(),
    )
    if (isContinuous) {
      val deploymentRegistry = services.get(DeploymentRegistry::class.java)
      val deploymentHandle = deploymentRegistry.get("jekyll", ContainerRunner.Handle::class.java)
      if (deploymentHandle == null) {
        deploymentRegistry.start(
          "jekyll",
          DeploymentRegistry.ChangeBehavior.BLOCK,
          ContainerRunner.Handle::class.java, runner
        )
      }
    } else {
      runner.execute(services).assertNormalExitValue()
    }
  }

  protected open fun beforeAction() {
    addContainerVolume(workingDir.asFile.get())
    addContainerVolume(project.rootDir.resolve(".git"))
  }

  protected open fun afterAction(): Unit = Unit

  protected open fun prepareCommandArgs(mode: JekyllMode): List<String> {
    return this.args.get()
  }

  protected open fun prepareContainerArgs(mode: JekyllMode): List<String> {
    val args = mutableListOf(
      "-i",
      "--rm",
      "--init",
      "-w=${workingDir.asFile.get().absolutePath}",
    )
    getContainerVolume(
      project.buildDir.resolve(".bundle/$name").also(File::mkdirs),
      File("/usr/local/bundle")
    )?.let(args::add)
    if (mode == PODMAN) args += "-e=JEKYLL_ROOTLESS=1"
    environment.get().forEach { (k, v) ->
      args += "-e=$k=$v"
    }
    args += containerVolumes.mapNotNull(::getContainerVolume)
    return args + containerArgs.get()
  }
}
