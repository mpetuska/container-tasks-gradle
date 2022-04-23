package dev.petuska.container.jekyll.task

import dev.petuska.container.task.ContainerExecTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.UntrackedTask
import org.gradle.api.tasks.options.Option
import java.io.File

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
    image.setFinal("docker.io/jekyll/jekyll")
    executable.setFinal("jekyll")
    addContainerVolume(
      project.provider { project.buildDir.resolve(".bundle/$name").also(File::mkdirs) },
      project.provider { File("/usr/local/bundle/") }
    )
  }

  protected override fun prepareCommandArgs(mode: Mode): List<String> {
    val args = super.prepareCommandArgs(mode).toMutableList()
    command?.let { args.add(0, it) }
    args += prepareJekyllArgs(mode)
    return args
  }

  protected open fun prepareJekyllArgs(mode: Mode): List<String> {
    val args = args.get().toMutableList()
    if (safe.getOrElse(false)) args += "--safe"
    return args
  }
}
