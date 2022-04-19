package dev.petuska.jekyll.config

import dev.petuska.jekyll.extension.JekyllExtension
import dev.petuska.jekyll.extension.domain.JekyllMode
import dev.petuska.jekyll.extension.domain.JekyllSourceSet
import dev.petuska.jekyll.extension.domain.JekyllSourceSets
import dev.petuska.jekyll.util.ProjectEnhancer
import dev.petuska.jekyll.util.toCamelCase
import java.io.OutputStream


internal fun ProjectEnhancer.configure(extension: JekyllExtension) {
  with(extension) {
    mode.convention(provider {
      fun hasExecutable(executable: String): Boolean = project.exec {
        it.isIgnoreExitValue = true
        it.commandLine("which", executable)
        it.environment(environment.get())
        it.errorOutput = OutputStream.nullOutputStream()
        it.standardOutput = OutputStream.nullOutputStream()
      }.exitValue == 0
      if (hasExecutable("jekyll")) {
        logger.info("Native Jekyll detected")
        JekyllMode.NATIVE
      } else if (hasExecutable("podman")) {
        logger.info("Podman detected. Using it to run Jekyll via a rootless container")
        JekyllMode.PODMAN
      } else if (hasExecutable("docker")) {
        logger.info("Docker detected. Using it to run Jekyll via a root container")
        JekyllMode.DOCKER
      } else {
        error("No jekyll, podman or docker executable found")
      }
    })
    version.convention("4.2.2")
    val sourceSets: JekyllSourceSets = objects.domainObjectContainer(JekyllSourceSet::class.java) {
      objects.newInstance(JekyllSourceSet::class.java, it, objects.sourceDirectorySet(it, it.toCamelCase()))
    }
    configure(sourceSets)
    extensions.add("sourceSets", sourceSets)
  }
}
