package dev.petuska.container.task

import dev.petuska.container.task.ContainerExecTask.Mode
import org.gradle.api.tasks.Internal

@Suppress("PropertyName")
public interface ContainerExecModeScope {
  @get:Internal
  public val NATIVE: Mode get() = Mode.NATIVE

  @get:Internal
  public val PODMAN: Mode get() = Mode.PODMAN

  @get:Internal
  public val DOCKER: Mode get() = Mode.DOCKER
}
