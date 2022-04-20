package dev.petuska.jekyll.extension.domain

import dev.petuska.jekyll.config.assembleTaskName
import dev.petuska.jekyll.config.buildTaskName
import dev.petuska.jekyll.config.bundleExecTaskName
import dev.petuska.jekyll.config.execTaskName
import dev.petuska.jekyll.config.initTaskName
import dev.petuska.jekyll.config.resourcesTaskName
import dev.petuska.jekyll.config.serveTaskName
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

@Suppress("MemberVisibilityCanBePrivate")
public abstract class JekyllSourceSet @Inject constructor(public val name: String, objectFactory: ObjectFactory) :
  JekyllCommonOptions {
  public val jekyll: SourceDirectorySet = objectFactory.sourceDirectorySet("jekyll", "$name Jekyll source")
  public val resources: SourceDirectorySet = objectFactory.sourceDirectorySet("resources", "$name resources")
  public val allSources: SourceDirectorySet = objectFactory.sourceDirectorySet("allsource", "$name sources")

  public val processResourcesTaskName: String = resourcesTaskName(name)
  public val assembleTaskName: String = assembleTaskName(name)
  public val buildTaskName: String = buildTaskName(name)
  public val initTaskName: String = initTaskName(name)
  public val execTaskName: String = execTaskName(name)
  public val bundleExecTaskName: String = bundleExecTaskName(name)
  public val serveTaskName: String = serveTaskName(name)

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
    )
  }
}

public typealias JekyllSourceSets = NamedDomainObjectContainer<JekyllSourceSet>
