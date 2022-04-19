package dev.petuska.jekyll.config

import dev.petuska.jekyll.task.BundleExecTask
import dev.petuska.jekyll.task.ContainerExecTask
import dev.petuska.jekyll.task.JekyllExecTask
import dev.petuska.jekyll.util.ProjectEnhancer
import org.gradle.api.tasks.TaskContainer

internal fun ProjectEnhancer.configure(tasks: TaskContainer) {
  with(tasks) {
    whenObjectAdded { task ->
      if (task is ContainerExecTask) {
        task.mode.convention(extension.mode)
        task.version.convention(extension.version)
        task.environment.convention(extension.environment)
      }
    }
    register("jekyllExec", JekyllExecTask::class.java) {
      it.outputs.upToDateWhen { false }
    }
    register("bundleExec", BundleExecTask::class.java) {
      it.outputs.upToDateWhen { false }
    }
  }
}
