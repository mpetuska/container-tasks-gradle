package dev.petuska.container.jekyll.task

import dev.petuska.container.task.ContainerExecTask
import org.gradle.api.tasks.UntrackedTask

@Suppress("LeakingThis")
@UntrackedTask(because = "Must always run")
public abstract class BundleExecTask : ContainerExecTask("bundle") {
  init {
    group = "bundle"
    description = "Executes bundle command"
    image.setFinal("docker.io/jekyll/jekyll")
    executable.setFinal("bundle")
  }
}
