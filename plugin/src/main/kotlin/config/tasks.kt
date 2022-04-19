package dev.petuska.jekyll.config

import dev.petuska.jekyll.task.JekyllExecTask
import dev.petuska.jekyll.util.ProjectEnhancer
import org.gradle.api.tasks.TaskContainer

internal fun ProjectEnhancer.configure(tasks: TaskContainer) {
  with(tasks) {
    whenObjectAdded { task ->
      if (task is JekyllExecTask) {
        task.mode.convention(extension.mode)
        task.version.convention(extension.version)
        task.environment.convention(extension.environment)
      }
    }
    register("jekyll", JekyllExecTask::class.java)
  }
}
