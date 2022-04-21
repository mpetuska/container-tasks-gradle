package dev.petuska.jekyll.config

import dev.petuska.jekyll.extension.domain.JekyllSourceSet
import dev.petuska.jekyll.extension.domain.JekyllSourceSets
import dev.petuska.jekyll.task.BundleExecTask
import dev.petuska.jekyll.task.JekyllBuildTask
import dev.petuska.jekyll.task.JekyllExecTask
import dev.petuska.jekyll.task.JekyllInitTask
import dev.petuska.jekyll.task.JekyllServeTask
import dev.petuska.jekyll.util.ProjectEnhancer
import org.gradle.api.Task
import org.gradle.api.internal.plugins.DslObject
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Copy
import org.gradle.language.jvm.tasks.ProcessResources
import java.io.File

internal fun ProjectEnhancer.configure(sourceSets: JekyllSourceSets) {
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

private fun <T : Task> Property<T>.setFinal(provider: Provider<T>) {
  set(provider)
  finalizeValue()
}

private inline fun <reified T : Task> ProjectEnhancer.registerTask(
  name: String,
  crossinline config: T.() -> Unit
) = tasks.register(name, T::class.java) {
  it.config()
}

private fun ProjectEnhancer.resourcesTask(sourceSet: JekyllSourceSet) =
  registerTask<ProcessResources>(sourceSet.processResourcesTaskName) {
    DslObject(this.rootSpec).conventionMapping.map("destinationDir") {
      sourceSet.resources.destinationDirectory.asFile.get()
    }
    from(sourceSet.resources)
  }

private fun ProjectEnhancer.assembleTask(sourceSet: JekyllSourceSet) =
  registerTask<Copy>(sourceSet.assembleTaskName) {
    dependsOn(sourceSet.processResourcesTaskName)
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

private fun ProjectEnhancer.buildTask(sourceSet: JekyllSourceSet) =
  registerTask<JekyllBuildTask>(sourceSet.buildTaskName) {
    dependsOn(sourceSet.assembleTaskName)
    source.convention(sourceSet.jekyll.classesDirectory)
    mode.convention(sourceSet.mode)
    version.convention(sourceSet.version)
    environment.convention(sourceSet.environment)
  }

private fun ProjectEnhancer.initTask(sourceSet: JekyllSourceSet) =
  registerTask<JekyllInitTask>(sourceSet.initTaskName) {
    workingDir.convention(layout.dir(provider { sourceSet.resources.srcDirs.first() }))
    mode.convention(sourceSet.mode)
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
      delete(jekyllDirs.filter { it.list().isEmpty() })
    }
  }

private fun ProjectEnhancer.bundleExecTask(sourceSet: JekyllSourceSet) =
  registerTask<BundleExecTask>(sourceSet.bundleExecTaskName) {
    workingDir.convention(layout.dir(provider { sourceSet.resources.srcDirs.first() }))
    mode.convention(sourceSet.mode)
    version.convention(sourceSet.version)
    environment.convention(sourceSet.environment)
  }

private fun ProjectEnhancer.execTask(sourceSet: JekyllSourceSet) =
  registerTask<JekyllExecTask>(sourceSet.execTaskName) {
    workingDir.convention(layout.dir(provider { sourceSet.resources.srcDirs.first() }))
    mode.convention(sourceSet.mode)
    version.convention(sourceSet.version)
    environment.convention(sourceSet.environment)
    outputs.upToDateWhen { false }
  }

private fun ProjectEnhancer.serveTask(sourceSet: JekyllSourceSet) =
  registerTask<JekyllServeTask>(sourceSet.serveTaskName) {
    dependsOn(sourceSet.assembleTaskName)
    source.convention(sourceSet.jekyll.classesDirectory)
    workingDir.convention(source)
    mode.convention(sourceSet.mode)
    version.convention(sourceSet.version)
    environment.convention(sourceSet.environment)
  }
