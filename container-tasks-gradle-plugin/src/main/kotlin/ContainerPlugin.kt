package dev.petuska.container

import dev.petuska.container.ContainerPlugin.Extension
import dev.petuska.container.task.ContainerExecModeScope
import dev.petuska.container.task.ContainerExecTask.Mode
import dev.petuska.container.util.ContainerProject
import org.gradle.api.Named
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

public abstract class ContainerPlugin<E : Extension> : Plugin<Project> {
  override fun apply(target: Project) {
    ContainerProject(
      extension = target.createExtension(),
      project = target,
    ).apply()
  }

  internal abstract fun Project.createExtension(): E
  internal abstract fun ContainerProject<E>.apply()

  public interface Extension : Named, ExtensionAware, ContainerExecModeScope {
    @Input
    override fun getName(): String

    @get:Input
    public val mode: Property<Mode>

    @get:Input
    @get:Optional
    public val image: Property<String>

    @get:Input
    @get:Optional
    public val version: Property<String>

    @get:Input
    public val environment: MapProperty<String, Any>
  }
}
