package dev.petuska.jekyll.task

import org.gradle.api.tasks.UntrackedTask

@Suppress("LeakingThis")
@UntrackedTask(because = "Must always run")
public abstract class BundleExecTask : ContainerExecTask("bundle") {
  init {
    group = "jekyll"
    description = "Executes bundle command"
  }
}
