package dev.petuska.jekyll.task

import dev.petuska.jekyll.extension.domain.JekyllMode
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.options.Option
import org.gradle.work.DisableCachingByDefault
import java.io.File
import java.net.ServerSocket

@Suppress("LeakingThis")
@DisableCachingByDefault(because = "Not worth caching")
public abstract class JekyllServeTask : JekyllBuildTask() {

  @get:Input
  public abstract val port: Property<Int>

  @Option(option = "port", description = "Listen on the given port. The default is `4000`.")
  public fun port(port: String) {
    this.port.set(port.toInt())
  }

  @get:Input
  @get:Optional
  @get:Option(option = "host", description = "Listen at the given hostname. The default is `localhost`.")
  public abstract val host: Property<String>

  @get:Input
  @get:Optional
  @get:Option(
    option = "livereload",
    description = "Reload a page automatically on the browser when its content is edited."
  )
  public abstract val liveReload: Property<Boolean>

  @get:Input
  @get:Optional
  @get:Option(option = "livereload-ignore", description = "File glob patterns for LiveReload to ignore.")
  public abstract val liveReloadIgnore: ListProperty<String>

  @get:Input
  @get:Optional
  public abstract val liveReloadMinDelay: Property<Int>

  @Option(option = "livereload-min-delay", description = "Minimum delay before automatically reloading page.")
  public fun liveReloadMinDelay(delay: String) {
    this.liveReloadMinDelay.set(delay.toInt())
  }

  @get:Input
  @get:Optional
  public abstract val liveReloadMaxDelay: Property<Int>

  @Option(option = "livereload-max-delay", description = "Maximum delay before automatically reloading page.")
  public fun liveReloadMaxDelay(delay: String) {
    this.liveReloadMinDelay.set(delay.toInt())
  }

  @get:Input
  @get:Optional
  public abstract val liveReloadPort: Property<Int>

  @Option(option = "livereload-port", description = "Port for LiveReload to listen on.")
  public fun liveReloadPort(port: String) {
    this.liveReloadPort.set(port.toInt())
  }

  @get:Input
  @get:Optional
  @get:Option(option = "open-url", description = "Open the site's URL in the browser.")
  public abstract val openUrl: Property<Boolean>

  @get:Input
  @get:Optional
  @get:Option(option = "detach", description = "Detach the server from the terminal.")
  public abstract val detach: Property<Boolean>

  @get:Input
  @get:Optional
  @get:Option(
    option = "skip-initial-build",
    description = "Skips the initial site build which occurs before the server is started."
  )
  public abstract val skipInitialBuild: Property<Boolean>

  @get:Input
  @get:Optional
  @get:Option(option = "show-dir-listing", description = "Show a directory listing instead of loading your index file.")
  public abstract val showDirListing: Property<Boolean>

  @get:Input
  @get:Optional
  public abstract val sslKey: RegularFileProperty

  @Option(option = "ssl-key", description = "SSL Private Key, stored or symlinked in the site source.")
  public fun sslKey(path: String) {
    sslKey.set(File(path))
  }

  @get:Input
  @get:Optional
  public abstract val sslCert: RegularFileProperty

  @Option(option = "ssl-cert", description = "SSL Public certificate, stored or symlinked in the site source.")
  public fun sslCert(path: String) {
    sslCert.set(File(path))
  }

  init {
    description = "Serves jekyll website"
    ignoreExitValue.convention(true)
    host.convention("localhost")
    port.convention(project.provider { findPort(4000) })
    liveReloadPort.convention(project.provider { findPort(35729) })
    liveReload.convention(true)
  }

  override val command: String = "serve"

  private fun findPort(desired: Int): Int {
    return runCatching {
      ServerSocket(desired).use {
        it.localPort
      }.takeIf { it > 0 }
    }.getOrNull() ?: ServerSocket(0).use {
      it.localPort
    }
  }

  override fun beforeAction() {
    super.beforeAction()
    if (sslKey.isPresent) addContainerVolume(sslKey.asFile.get())
    if (sslCert.isPresent) addContainerVolume(sslCert.asFile.get())
  }

  override fun prepareJekyllArgs(mode: JekyllMode): List<String> {
    val args = mutableListOf<String>()
    args += listOf("--port", "${port.get()}")
    if (host.isPresent) args += listOf("--host", host.get())
    if (liveReload.getOrElse(false)) args += "--livereload"
    if (liveReloadIgnore.isPresent) liveReloadIgnore.get().joinToString(",").takeIf(String::isNotBlank)?.let {
      args += listOf("--livereload-ignore", it)
    }
    if (liveReloadMinDelay.isPresent) args += listOf("--livereload-min-delay", "${liveReloadMinDelay.get()}")
    if (liveReloadMaxDelay.isPresent) args += listOf("--livereload-max-delay", "${liveReloadMaxDelay.get()}")
    if (liveReloadPort.isPresent) args += listOf("--livereload-port", "${liveReloadPort.get()}")
    if (openUrl.getOrElse(false)) args += "--open-url"
    if (detach.getOrElse(false)) args += "--detach"
    if (skipInitialBuild.getOrElse(false)) args += "--skip-initial-build"
    if (showDirListing.getOrElse(false)) args += "--show-dir-listing"
    if (sslKey.isPresent) args += listOf("--ssl-key", sslKey.asFile.get().absolutePath)
    if (sslCert.isPresent) args += listOf("--ssl-cert", sslCert.asFile.get().absolutePath)
    return super.prepareJekyllArgs(mode) + args
  }

  override fun prepareContainerArgs(mode: JekyllMode): List<String> {
    val args = mutableListOf("--network=host")
    if (host.isPresent) args += "--hostname=${host.get()}"
    return super.prepareContainerArgs(mode) + args
  }
}
