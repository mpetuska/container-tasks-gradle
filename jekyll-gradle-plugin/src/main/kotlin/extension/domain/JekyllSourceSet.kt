package dev.petuska.jekyll.extension.domain

import dev.petuska.jekyll.config.assembleTaskName
import dev.petuska.jekyll.config.buildTaskName
import dev.petuska.jekyll.config.bundleExecTaskName
import dev.petuska.jekyll.config.execTaskName
import dev.petuska.jekyll.config.initTaskName
import dev.petuska.jekyll.config.resourcesTaskName
import dev.petuska.jekyll.config.serveTaskName
import dev.petuska.jekyll.task.BundleExecTask
import dev.petuska.jekyll.task.JekyllBuildTask
import dev.petuska.jekyll.task.JekyllExecTask
import dev.petuska.jekyll.task.JekyllInitTask
import dev.petuska.jekyll.task.JekyllServeTask
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Copy
import org.gradle.language.jvm.tasks.ProcessResources
import javax.inject.Inject

@Suppress("MemberVisibilityCanBePrivate")
public abstract class JekyllSourceSet @Inject constructor(public val name: String, objectFactory: ObjectFactory) :
  JekyllCommonOptions {
  public val jekyll: SourceDirectorySet = objectFactory.sourceDirectorySet("jekyll", "$name Jekyll source")
  public val resources: SourceDirectorySet = objectFactory.sourceDirectorySet("resources", "$name resources")
  public val allSources: SourceDirectorySet = objectFactory.sourceDirectorySet("allsource", "$name sources")

  public val processResourcesTaskName: String = resourcesTaskName(name)
  public abstract val processResourcesTask: Property<ProcessResources>
  public val assembleTaskName: String = assembleTaskName(name)
  public abstract val assembleTask: Property<Copy>
  public val buildTaskName: String = buildTaskName(name)
  public abstract val buildTask: Property<JekyllBuildTask>
  public val initTaskName: String = initTaskName(name)
  public abstract val initTask: Property<JekyllInitTask>
  public val execTaskName: String = execTaskName(name)
  public abstract val execTask: Property<JekyllExecTask>
  public val bundleExecTaskName: String = bundleExecTaskName(name)
  public abstract val bundleExecTask: Property<BundleExecTask>
  public val serveTaskName: String = serveTaskName(name)
  public abstract val serveTask: Property<JekyllServeTask>

  init {
    jekyllSourcePatterns.forEach(jekyll.filter::include)
    resources.exclude {
      jekyll.contains(it.file)
    }
    allSources.source(resources)
    allSources.source(jekyll)
  }

  public companion object {
    internal val jekyllSourcePatterns: Set<String> = setOf(
      "**/*.md",
      "**/*.MD",
      "**/*.markdown",
      "**/*.MARKDOWN",
      "**/*.html",
      "**/*.HTML",
      "**/*.liquid",
      "**/*.LIQUID",
      "**/*.js",
    )
  }
}

public typealias JekyllSourceSets = NamedDomainObjectContainer<JekyllSourceSet>
