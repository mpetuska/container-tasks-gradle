package dev.petuska.jekyll.extension.domain

import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.file.SourceDirectorySet
import javax.inject.Inject

public abstract class JekyllSourceSet @Inject constructor(public val sources: SourceDirectorySet) : Named,
  JekyllCommonOptions

public typealias JekyllSourceSets = NamedDomainObjectContainer<JekyllSourceSet>
