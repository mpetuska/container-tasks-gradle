package dev.petuska.jekyll.task

import org.gradle.work.DisableCachingByDefault

@Suppress("LeakingThis")
@DisableCachingByDefault(because = "Not worth caching")
public abstract class BundleExecTask : ContainerExecTask("bundle") {
  init {
    group = "jekyll"
    description = "Executes bundle command"
  }
}
