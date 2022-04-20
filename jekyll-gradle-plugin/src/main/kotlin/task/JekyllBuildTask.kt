package dev.petuska.jekyll.task

import dev.petuska.jekyll.extension.domain.JekyllMode
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.options.Option
import java.io.File
import java.net.URI

@Suppress("LeakingThis")
public abstract class JekyllBuildTask : JekyllExecTask() {
  @get:Input
  @get:Optional
  @get:Option(option = "--disable-disk-cache", description = "Disable caching to disk in non-safe mode")
  public abstract val disableBuildCache: Property<Boolean>

  @get:Input
  @get:Optional
  @get:Option(option = "watch", description = "Enable auto-regeneration of the site when files are modified.")
  public abstract val watch: Property<Boolean>

  @get:InputFiles
  @get:Optional
  public abstract val config: ConfigurableFileCollection

  @Option(
    option = "config",
    description = "Specify config files instead of using _config.yml automatically. " + "Settings in later files override settings in earlier files."
  )
  public fun config(paths: List<String>) {
    paths.map(::File).forEach(config::from)
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
  @get:Option(option = "drafts", description = "Process and render draft posts.")
  public abstract val drafts: Property<Boolean>

  @get:Input
  @get:Optional
  @get:Option(option = "future", description = "Publish posts or collection documents with a future date.")
  public abstract val future: Property<Boolean>

  @get:Input
  @get:Optional
  @get:Option(option = "unpublished", description = "Render posts that were marked as unpublished.")
  public abstract val unpublished: Property<Boolean>

  @get:Input
  @get:Optional
  @get:Option(
    option = "lsi", description = "Produce an index for related posts. Requires the classifier-reborn plugin."
  )
  public abstract val lsi: Property<Boolean>

  @get:Input
  @get:Optional
  @get:Option(option = "limit_posts", description = "Limit the number of posts to parse and publish.")
  public abstract val limitPosts: Property<Boolean>

  @get:Input
  @get:Optional
  @get:Option(option = "force_polling", description = "Force watch to use polling.")
  public abstract val forcePolling: Property<Boolean>

  @get:Input
  @get:Optional
  @get:Option(option = "verbose", description = "Print verbose output.")
  public abstract val verbose: Property<Boolean>

  @get:Input
  @get:Optional
  @get:Option(option = "quiet", description = "Silence the normal output from Jekyll during a build.")
  public abstract val quiet: Property<Boolean>

  @get:Input
  @get:Optional
  @get:Option(
    option = "incremental",
    description = "Enable the experimental incremental build feature. " + "Incremental build only re-builds posts and pages that have changed, " + "resulting in significant performance improvements for large sites, " + "but may also break site generation in certain cases."
  )
  public abstract val incremental: Property<Boolean>

  @get:Input
  @get:Optional
  @get:Option(
    option = "profile",
    description = "Generate a Liquid rendering profile to help you identify performance bottlenecks."
  )
  public abstract val profile: Property<Boolean>

  @get:Input
  @get:Optional
  @get:Option(
    option = "strict_front_matter",
    description = "Cause a build to fail if there is a YAML syntax error in a page's front matter."
  )
  public abstract val strictFrontMatter: Property<Boolean>

  @get:Input
  @get:Optional
  public abstract val baseurl: Property<URI>

  @Option(option = "baseurl", description = "Serve the website from the given base URL.")
  public fun baseurl(url: String) {
    baseurl.set(URI(url))
  }

  @get:Input
  @get:Optional
  @get:Option(option = "trace", description = "Show the full backtrace when an error occurs.")
  public abstract val trace: Property<Boolean>

  @get:InputDirectory
  public abstract val source: DirectoryProperty

  @Option(option = "source", description = "Change the directory where Jekyll will read files")
  public fun source(path: String) {
    source.set(File(path))
  }

  @get:OutputDirectory
  public abstract val destination: DirectoryProperty

  @Option(option = "destination", description = "Change the directory where Jekyll will write files")
  public fun destination(path: String) {
    destination.set(File(path))
  }

  init {
    description = "Builds jekyll website"
    destination.convention(workingDir.dir("_site"))
    disableBuildCache.convention(true)
  }

  override val command: String = "build"

  override fun beforeAction() {
    super.beforeAction()
    if (destination.isPresent) addContainerVolume(destination.asFile.get())
    if (source.isPresent) addContainerVolume(source.asFile.get())
    config.forEach(::addContainerVolume)
    plugins.forEach(::addContainerVolume)
    layouts.asFile.orNull?.let(::addContainerVolume)
  }

  override fun prepareJekyllArgs(mode: JekyllMode): List<String> {
    val args = super.prepareJekyllArgs(mode).toMutableList()
    if (disableBuildCache.getOrElse(false)) args += "--disable-disk-cache"
    if (watch.getOrElse(false)) args += "--watch"
    if (watch.getOrElse(false)) args += "--drafts"
    if (future.getOrElse(false)) args += "--future"
    if (unpublished.getOrElse(false)) args += "--unpublished"
    if (lsi.getOrElse(false)) args += "--lsi"
    if (limitPosts.getOrElse(false)) args += "--limit_posts"
    if (forcePolling.getOrElse(false)) args += "--force_polling"
    if (verbose.getOrElse(false)) args += "--verbose"
    if (quiet.getOrElse(false)) args += "--quiet"
    if (incremental.getOrElse(false)) args += "--incremental"
    if (profile.getOrElse(false)) args += "--profile"
    if (strictFrontMatter.getOrElse(false)) args += "--strict_front_matter"
    if (baseurl.isPresent) args += listOf("--baseurl", baseurl.toString())
    if (trace.getOrElse(false)) args += "--trace"
    if (source.isPresent) args += listOf("--source", source.asFile.get().absolutePath)
    if (destination.isPresent) args += listOf("--destination", destination.asFile.get().absolutePath)
    config.joinToString(",", transform = File::getAbsolutePath).takeIf(String::isNotBlank)?.let {
      args += listOf("--config", it)
    }
    plugins.joinToString(",", transform = File::getAbsolutePath).takeIf(String::isNotBlank)?.let {
      args += listOf("--plugins", it)
    }
    if (layouts.isPresent) args += listOf("--layouts", layouts.asFile.get().absolutePath)
    return args
  }
}
