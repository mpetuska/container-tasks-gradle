package dev.petuska.container.jekyll.task

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.UntrackedTask
import org.gradle.api.tasks.options.Option
import java.io.File

@Suppress("LeakingThis")
@UntrackedTask(because = "Must always run")
public abstract class JekyllInitTask : JekyllExecTask() {
  @get:Input
  @get:Optional
  @get:Option(option = "force", description = "Force creation even if PATH already exists")
  public abstract val force: Property<Boolean>

  @get:Input
  @get:Optional
  @get:Option(option = "blank", description = "Creates scaffolding but with empty files")
  public abstract val blank: Property<Boolean>

  @get:Input
  @get:Optional
  @get:Option(option = "skip-bundle", description = "Skip 'bundle install'")
  public abstract val skipBundle: Property<Boolean>

  @get:Input
  @get:Optional
  @get:Option(option = "trace", description = "Show the full backtrace when an error occurs.")
  public abstract val trace: Property<Boolean>

  @get:InputDirectory
  @get:Optional
  public abstract val source: DirectoryProperty

  @Option(option = "source", description = "Source directory (defaults to ./)")
  public fun source(path: String) {
    source.set(File(path))
  }

  @get:InputDirectory
  @get:Optional
  public abstract val destination: DirectoryProperty

  @Option(option = "destination", description = "Destination directory (defaults to ./_site)")
  public fun destination(path: String) {
    destination.set(File(path))
  }

  @get:InputFiles
  @get:Optional
  public abstract val plugins: ConfigurableFileCollection

  @Option(option = "plugins", description = "Specify plugin directories instead of using _plugins/ automatically.")
  public fun plugins(paths: List<String>) {
    paths.map(::File).forEach(plugins::from)
  }

  @get:InputDirectory
  @get:Optional
  public abstract val layouts: DirectoryProperty

  @Option(option = "layouts", description = "Specify layout directory instead of using _layouts/ automatically.")
  public fun plugins(path: String) {
    layouts.set(File(path))
  }

  @get:Input
  @get:Optional
  @get:Option(
    option = "profile",
    description = "Generate a Liquid rendering profile to help you identify performance bottlenecks."
  )
  public abstract val profile: Property<Boolean>

  init {
    description = "Initialises jekyll website"
    addContainerVolume(source.asFile)
    addContainerVolume(destination.asFile)
    addContainerVolume(layouts.asFile)
    plugins.map { project.provider { it } }.forEach(::addContainerVolume)
    workingDir.convention(source)
  }

  override val command: String = "new"

  override fun beforeAction() {
    super.beforeAction()
    if (force.getOrElse(false)) workingDir.asFile.get().run {
      deleteRecursively()
      mkdirs()
    }
  }

  override fun prepareJekyllArgs(mode: Mode): List<String> {
    val args = mutableListOf(".")
    if (force.getOrElse(false)) args += "--force"
    if (blank.getOrElse(false)) args += "--blank"
    if (skipBundle.getOrElse(false)) args += "--skip-bundle"
    if (trace.getOrElse(false)) args += "--trace"
    if (source.isPresent) args += listOf("--source", source.asFile.get().absolutePath)
    if (destination.isPresent) args += listOf("--destination", destination.asFile.get().absolutePath)
    if (profile.getOrElse(false)) args += "--profile"
    plugins.joinToString(",", transform = File::getAbsolutePath).takeIf(String::isNotBlank)?.let {
      args += listOf("--plugins", it)
    }
    if (layouts.isPresent) args += listOf("--layouts", layouts.asFile.get().absolutePath)
    return super.prepareJekyllArgs(mode) + args
  }

  override fun afterAction() {
    super.afterAction()
    println("[jekyll] Jekyll site initialised in ${workingDir.asFile.get().absolutePath}")
  }
}
