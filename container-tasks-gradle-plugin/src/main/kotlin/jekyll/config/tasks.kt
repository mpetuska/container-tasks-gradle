package dev.petuska.container.jekyll.config

import dev.petuska.container.jekyll.JekyllProject
import dev.petuska.container.jekyll.task.BundleExecTask
import dev.petuska.container.jekyll.task.JekyllExecTask
import dev.petuska.container.task.ContainerExecTask
import org.gradle.api.tasks.TaskContainer

internal fun JekyllProject.configure(tasks: TaskContainer) {
  with(tasks) {
    whenObjectAdded { task ->
      if (task is ContainerExecTask) {
        task.version.convention(extension.version)
        task.environment.convention(extension.environment)
      }
      if (task is BundleExecTask) task.mode.convention(extension.mode.orElse(modeProvider("bundle")))
      if (task is JekyllExecTask) task.mode.convention(extension.mode.orElse(modeProvider("jekyll")))
    }
    register("jekyllExec", JekyllExecTask::class.java)
    register("bundleExec", BundleExecTask::class.java)
  }
}
