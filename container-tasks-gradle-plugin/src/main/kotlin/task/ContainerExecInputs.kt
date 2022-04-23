package dev.petuska.container.task

import dev.petuska.container.ContainerPlugin
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.options.Option
import java.io.File

@Suppress("LeakingThis", "TooManyFunctions")
public interface ContainerExecInputs : ContainerExecModeScope, ContainerPlugin.Extension {
  @get:Input
  @get:Option(option = "executable", description = "Executable to be invoked")
  public val executable: Property<String>

  @get:Input
  @get:Optional
  public val ignoreExitValue: Property<Boolean>

  @get:Input
  public val args: ListProperty<String>

  @Option(option = "arg", description = "Argument to be passed to the native executable")
  public fun arg(args: List<String>) {
    this.args.addAll(args)
  }

  @Option(option = "args", description = "Arguments to be passed to the native executable")
  public fun args(args: String) {
    this.args.addAll(args.split(" "))
  }

  @get:Input
  public val containerArgs: ListProperty<String>

  @get:Input
  public val containerVolumes: MapProperty<File, File>

  @Option(
    option = "volume",
    description = "Container volume to be mounted during execution in a form of `/host/path` or `/host/path:/container/path"
  )
  public fun containerVolume(volume: String) {
    val chunks = volume.split(":")
    when (chunks.size) {
      1 -> File(chunks[0]).let { containerVolumes.put(it, it) }
      2 -> containerVolumes.put(File(chunks[0]), File(chunks[1]))
      else -> error("`$volume` is invalid! More than one `:` separator present")
    }
  }

  @get:Internal
  public val workingDir: DirectoryProperty
}
