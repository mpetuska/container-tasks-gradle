package dev.petuska.container.jekyll.config

import dev.petuska.container.jekyll.JekyllProject
import dev.petuska.container.jekyll.extension.domain.JekyllSourceSet
import dev.petuska.container.jekyll.extension.domain.JekyllSourceSets
import dev.petuska.container.jekyll.task.BundleExecTask
import dev.petuska.container.jekyll.task.JekyllBuildTask
import dev.petuska.container.jekyll.task.JekyllExecTask
import dev.petuska.container.jekyll.task.JekyllInitTask
import dev.petuska.container.jekyll.task.JekyllServeTask
import org.gradle.api.Task
import org.gradle.api.internal.plugins.DslObject
import org.gradle.api.tasks.Copy
import org.gradle.language.jvm.tasks.ProcessResources
import java.io.File

internal fun JekyllProject.configure(sourceSets: JekyllSourceSets) {
  with(sourceSets) {
    whenObjectAdded { sourceSet ->
      configure(sourceSet)
      val resourcesTask = resourcesTask(sourceSet)
      sourceSet.resources.compiledBy(resourcesTask) {
        objects.directoryProperty()
          .convention(project.layout.buildDirectory.dir(it.destinationDir.absolutePath))
      }
      sourceSet.processResourcesTask.setFinal(resourcesTask)
      val assembleTask = assembleTask(sourceSet)
      sourceSet.jekyll.compiledBy(assembleTask) {
        objects.directoryProperty()
          .convention(project.layout.buildDirectory.dir(it.destinationDir.absolutePath))
      }
      sourceSet.assembleTask.setFinal(assembleTask)
      val buildTask = buildTask(sourceSet)
      sourceSet.allSources.compiledBy(buildTask, JekyllBuildTask::destination)
      sourceSet.buildTask.setFinal(buildTask)
      sourceSet.serveTask.setFinal(serveTask(sourceSet))
      sourceSet.execTask.setFinal(execTask(sourceSet))
      sourceSet.bundleExecTask.setFinal(bundleExecTask(sourceSet))
      sourceSet.initTask.setFinal(initTask(sourceSet))
    }
    register("main")
    whenObjectRemoved { sourceSet ->
      tasks.findByName(buildTaskName(sourceSet.name))?.let(tasks::remove)
      tasks.findByName(serveTaskName(sourceSet.name))?.let(tasks::remove)
      tasks.findByName(execTaskName(sourceSet.name))?.let(tasks::remove)
      tasks.findByName(bundleExecTaskName(sourceSet.name))?.let(tasks::remove)
      tasks.findByName(initTaskName(sourceSet.name))?.let(tasks::remove)
    }
  }
}

private inline fun <reified T : Task> JekyllProject.registerTask(
  name: String,
  crossinline config: T.() -> Unit
) = tasks.register(name, T::class.java) {
  it.config()
}

private fun JekyllProject.resourcesTask(sourceSet: JekyllSourceSet) =
  registerTask<ProcessResources>(sourceSet.processResourcesTaskName) {
    DslObject(this.rootSpec).conventionMapping.map("destinationDir") {
      sourceSet.resources.destinationDirectory.asFile.get()
    }
    from(sourceSet.resources)
  }

private fun JekyllProject.assembleTask(sourceSet: JekyllSourceSet) =
  registerTask<Copy>(sourceSet.assembleTaskName) {
    dependsOn(sourceSet.processResourcesTask)
    DslObject(this.rootSpec).conventionMapping.map("destinationDir") {
      sourceSet.jekyll.destinationDirectory.asFile.get()
    }
    from(sourceSet.resources.classesDirectory)
    from(sourceSet.jekyll) { cp ->
      cp.rename {
        if (it.endsWith(".liquid", ignoreCase = true))
          it.replace(".liquid", ".html", ignoreCase = true)
        else it
      }
    }
  }

private fun JekyllProject.buildTask(sourceSet: JekyllSourceSet) =
  registerTask<JekyllBuildTask>(sourceSet.buildTaskName) {
    dependsOn(sourceSet.assembleTask)
    workingDir.convention(project.layout.dir(sourceSet.assembleTask.map(Copy::getDestinationDir)))
    destination.convention(sourceSet.allSources.destinationDirectory)
    mode.convention(sourceSet.mode.orElse(modeProvider("jekyll")))
    version.convention(sourceSet.version)
    environment.convention(sourceSet.environment)
  }

private fun JekyllProject.initTask(sourceSet: JekyllSourceSet) =
  registerTask<JekyllInitTask>(sourceSet.initTaskName) {
    source.convention(layout.dir(provider { sourceSet.resources.srcDirs.first() }))
    mode.convention(sourceSet.mode.orElse(modeProvider("jekyll")))
    version.convention(sourceSet.version)
    environment.convention(sourceSet.environment)
    doLast {
      val jekyllFiles = workingDir.asFileTree.matching {
        JekyllSourceSet.jekyllSourcePatterns.forEach(it::include)
      }
      val jekyllDirs = jekyllFiles.map(File::getParentFile)
      val jekyllSources = sourceSet.jekyll.srcDirs.first()
      if (force.getOrElse(false)) delete(jekyllSources)
      copy { cp ->
        cp.from(jekyllFiles)
        cp.into(jekyllSources)
      }
      copy { cp ->
        cp.from(workingDir.file(".gitignore"))
        cp.into(workingDir.asFile.get().parentFile)
      }
      delete(jekyllFiles, workingDir.file(".gitignore"))
      delete(jekyllDirs.filter { it.list()?.isEmpty() ?: false })
    }
  }

private fun JekyllProject.bundleExecTask(sourceSet: JekyllSourceSet) =
  registerTask<BundleExecTask>(sourceSet.bundleExecTaskName) {
    workingDir.convention(layout.dir(provider { sourceSet.resources.srcDirs.first() }))
    mode.convention(sourceSet.mode.orElse(modeProvider("jekyll")))
    version.convention(sourceSet.version)
    environment.convention(sourceSet.environment)
  }

private fun JekyllProject.execTask(sourceSet: JekyllSourceSet) =
  registerTask<JekyllExecTask>(sourceSet.execTaskName) {
    workingDir.convention(layout.dir(provider { sourceSet.resources.srcDirs.first() }))
    mode.convention(sourceSet.mode.orElse(modeProvider("jekyll")))
    version.convention(sourceSet.version)
    environment.convention(sourceSet.environment)
    outputs.upToDateWhen { false }
  }

private fun JekyllProject.serveTask(sourceSet: JekyllSourceSet) =
  registerTask<JekyllServeTask>(sourceSet.serveTaskName) {
    dependsOn(sourceSet.assembleTask)
    workingDir.convention(project.layout.dir(sourceSet.assembleTask.map(Copy::getDestinationDir)))
    mode.convention(sourceSet.mode.orElse(modeProvider("jekyll")))
    version.convention(sourceSet.version)
    environment.convention(sourceSet.environment)
  }
