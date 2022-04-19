package dev.petuska.jekyll.config

import dev.petuska.jekyll.extension.JekyllExtension
import dev.petuska.jekyll.extension.domain.JekyllSourceSet
import dev.petuska.jekyll.extension.domain.JekyllSourceSets
import dev.petuska.jekyll.util.ProjectEnhancer
import dev.petuska.jekyll.util.toCamelCase

internal fun ProjectEnhancer.configure(extension: JekyllExtension) {
  with(extension) {
    version.convention("latest")
    val sourceSets: JekyllSourceSets = objects.domainObjectContainer(JekyllSourceSet::class.java) { name ->
      objects.newInstance(JekyllSourceSet::class.java, name, objects.sourceDirectorySet(name, name.toCamelCase()))
    }
    extensions.add("sourceSets", sourceSets)
    configure(sourceSets)
  }
}
