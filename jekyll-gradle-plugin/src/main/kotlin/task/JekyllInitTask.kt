package dev.petuska.jekyll.task

import dev.petuska.jekyll.extension.domain.JekyllMode
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.options.Option
import java.io.File

@Suppress("LeakingThis")
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
    skipBundle.convention(true)
    outputs.upToDateWhen { false }
  }

  override val command: String = "new"

  override fun beforeAction() {
    super.beforeAction()
    workingDir.asFile.get().mkdirs()
    if (source.isPresent) setContainerVolume(source.get().asFile, containerDestination)
    if (destination.isPresent) setContainerVolume(destination.get().asFile, containerDestination)
    plugins.forEach {
      setContainerVolume(it, containerRoot.resolve("_plugins/${it.name}"))
    }
    layouts.asFile.orNull?.let {
      setContainerVolume(it, containerRoot.resolve("_layouts/${it.name}"))
    }
  }

  override fun prepareJekyllArgs(mode: JekyllMode): List<String> {
    val args = mutableListOf(".")
    if (force.getOrElse(false)) args += "--force"
    if (blank.getOrElse(false)) args += "--blank"
    if (skipBundle.getOrElse(false)) args += "--skip-bundle"
    if (trace.getOrElse(false)) args += "--trace"
    if (source.isPresent) containerPath(source.get().asFile)?.let { args += listOf("--destination", it) }
    if (destination.isPresent) containerPath(destination.get().asFile)?.let { args += listOf("--destination", it) }
    if (profile.getOrElse(false)) args += "--profile"
    plugins.mapNotNull(::containerPath).joinToString(",").takeIf(String::isNotBlank)?.let {
      args += listOf("--plugins", it)
    }
    if (layouts.isPresent) containerPath(layouts.asFile.get())?.let {
      args += listOf("--layouts", it)
    }
    return super.prepareJekyllArgs(mode) + args
  }

  override fun afterAction() {
    super.afterAction()
    println("[jekyll] Jekyll site initialised in ${workingDir.asFile.get().absolutePath}")
  }
}
