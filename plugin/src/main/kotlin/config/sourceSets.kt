package dev.petuska.jekyll.config

import dev.petuska.jekyll.extension.domain.JekyllSourceSets
import dev.petuska.jekyll.task.JekyllBuildTask
import dev.petuska.jekyll.task.JekyllExecTask
import dev.petuska.jekyll.task.JekyllInitTask
import dev.petuska.jekyll.task.JekyllServeTask
import dev.petuska.jekyll.util.ProjectEnhancer

internal fun ProjectEnhancer.configure(sourceSets: JekyllSourceSets) {
  with(sourceSets) {
    whenObjectAdded { sourceSet ->
      configure(sourceSet)
      val buildTask = tasks.register(buildTaskName(sourceSet.name), JekyllBuildTask::class.java) { task ->
        task.source.from(sourceSet.sources)
        task.destination.convention(layout.buildDirectory.dir("jekyll/${sourceSet.name}"))
        task.mode.convention(sourceSet.mode)
        task.version.convention(sourceSet.version)
        task.environment.convention(sourceSet.environment)
      }
      tasks.register(serveTaskName(sourceSet.name), JekyllServeTask::class.java) { task ->
        task.source.from(sourceSet.sources)
        task.mode.convention(sourceSet.mode)
        task.version.convention(sourceSet.version)
        task.environment.convention(sourceSet.environment)
      }
      tasks.register(execTaskName(sourceSet.name), JekyllExecTask::class.java) { task ->
        task.source.from(sourceSet.sources)
        task.mode.convention(sourceSet.mode)
        task.version.convention(sourceSet.version)
        task.environment.convention(sourceSet.environment)
      }
      tasks.register(initTaskName(sourceSet.name), JekyllInitTask::class.java) { task ->
        task.source.from(sourceSet.sources)
        task.workingDir.set(layout.dir(provider { sourceSet.sources.srcDirs.first() }))
        task.mode.convention(sourceSet.mode)
        task.version.convention(sourceSet.version)
        task.environment.convention(sourceSet.environment)
      }
      sourceSet.sources.compiledBy(buildTask, JekyllBuildTask::destination)
    }
    register("main") {
      it.sources.srcDir(layout.projectDirectory.dir("doc/main"))
    }
    whenObjectRemoved { sourceSet ->
      tasks.findByName(buildTaskName(sourceSet.name))?.let(tasks::remove)
      tasks.findByName(serveTaskName(sourceSet.name))?.let(tasks::remove)
      tasks.findByName(execTaskName(sourceSet.name))?.let(tasks::remove)
      tasks.findByName(initTaskName(sourceSet.name))?.let(tasks::remove)
    }
  }
}
