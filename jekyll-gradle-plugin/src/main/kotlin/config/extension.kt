package dev.petuska.jekyll.config

import dev.petuska.jekyll.extension.JekyllExtension
import dev.petuska.jekyll.extension.domain.JekyllSourceSet
import dev.petuska.jekyll.extension.domain.JekyllSourceSets
import dev.petuska.jekyll.util.ProjectEnhancer

internal fun ProjectEnhancer.configure(extension: JekyllExtension) {
  with(extension) {
    version.convention("latest")
    environment.convention(System.getenv())
    val sourceSets: JekyllSourceSets = objects.domainObjectContainer(JekyllSourceSet::class.java) { name ->
      objects.newInstance(JekyllSourceSet::class.java, name, objects)
    }
    extensions.add("sourceSets", sourceSets)
    configure(sourceSets)
  }
}
