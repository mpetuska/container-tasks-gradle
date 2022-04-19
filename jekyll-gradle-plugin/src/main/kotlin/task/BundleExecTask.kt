package dev.petuska.jekyll.task

@Suppress("LeakingThis")
public abstract class BundleExecTask : ContainerExecTask("bundle") {
  init {
    group = "jekyll"
    description = "Executes bundle command"
  }
}
