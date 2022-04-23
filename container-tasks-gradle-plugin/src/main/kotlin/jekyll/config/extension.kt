package dev.petuska.container.jekyll.config

import dev.petuska.container.jekyll.JekyllProject
import dev.petuska.container.jekyll.extension.JekyllExtension
import dev.petuska.container.jekyll.extension.domain.JekyllSourceSet
import dev.petuska.container.jekyll.extension.domain.JekyllSourceSets

internal fun JekyllProject.configure(extension: JekyllExtension) {
  with(extension) {
    version.convention("latest")
    val sourceSets: JekyllSourceSets = objects.domainObjectContainer(JekyllSourceSet::class.java) { name ->
      objects.newInstance(JekyllSourceSet::class.java, name, objects)
    }
    extensions.add("sourceSets", sourceSets)
    configure(sourceSets)
  }
}
