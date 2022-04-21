package dev.petuska.jekyll.task

import dev.petuska.jekyll.extension.domain.JekyllMode
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.UntrackedTask
import org.gradle.api.tasks.options.Option

@Suppress("LeakingThis")
@UntrackedTask(because = "Must always run")
public abstract class JekyllExecTask : ContainerExecTask("jekyll") {
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
    description = "Executes jekyll command"
    workingDir.convention(project.layout.dir(project.provider { temporaryDir.resolve("sources") }))
  }

  protected override fun beforeAction() {
    super.beforeAction()
    addContainerVolume(workingDir.asFile.get())
  }

  protected override fun prepareCommandArgs(mode: JekyllMode): List<String> {
    val args = super.prepareCommandArgs(mode).toMutableList()
    command?.let { args.add(0, it) }
    args += prepareJekyllArgs(mode)
    return args
  }

  protected open fun prepareJekyllArgs(mode: JekyllMode): List<String> {
    val args = args.get().toMutableList()
    if (safe.getOrElse(false)) args += "--safe"
    return args
  }
}
