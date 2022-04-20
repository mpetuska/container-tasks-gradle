package dev.petuska.jekyll.config

import dev.petuska.jekyll.extension.domain.JekyllSourceSet
import dev.petuska.jekyll.util.ProjectEnhancer
import dev.petuska.jekyll.util.toCamelCase

internal fun ProjectEnhancer.configure(sourceSet: JekyllSourceSet) {
  with(sourceSet) {
    mode.convention(extension.mode)
    version.convention(extension.version)
    environment.convention(extension.environment)

    jekyll.srcDir(layout.projectDirectory.dir("doc/${sourceSet.name}/jekyll"))
    jekyll.destinationDirectory.convention(layout.buildDirectory.dir("jekyll/${sourceSet.name}/source"))

    resources.srcDir(layout.projectDirectory.dir("doc/${sourceSet.name}/resources"))
    resources.destinationDirectory.convention(layout.buildDirectory.dir("resources/jekyll/${sourceSet.name}"))

    allSources.destinationDirectory.convention(layout.buildDirectory.dir("jekyll/${sourceSet.name}/site"))
  }
}

internal fun resourcesTaskName(sourceSetName: String): String = "jekyll${sourceSetName.toCamelCase()}ProcessResources"
internal fun assembleTaskName(sourceSetName: String): String = "jekyll${sourceSetName.toCamelCase()}Assemble"
internal fun buildTaskName(sourceSetName: String): String = "jekyll${sourceSetName.toCamelCase()}Build"
internal fun serveTaskName(sourceSetName: String): String = "jekyll${sourceSetName.toCamelCase()}Serve"
internal fun execTaskName(sourceSetName: String): String = "jekyll${sourceSetName.toCamelCase()}Exec"
internal fun initTaskName(sourceSetName: String): String = "jekyll${sourceSetName.toCamelCase()}Init"
internal fun bundleExecTaskName(sourceSetName: String): String = "bundle${sourceSetName.toCamelCase()}Exec"
