package dev.petuska.container.jekyll

import dev.petuska.container.ContainerPlugin
import dev.petuska.container.jekyll.config.configure
import dev.petuska.container.jekyll.extension.JekyllExtension
import dev.petuska.container.util.ContainerProject
import org.gradle.api.Project

internal typealias JekyllProject = ContainerProject<JekyllExtension>

public class JekyllPlugin : ContainerPlugin<JekyllExtension>() {
  override fun JekyllProject.apply() {
    configure(tasks)
    configure(extension)
  }

  override fun Project.createExtension(): JekyllExtension =
    extensions.create(JekyllExtension.NAME, JekyllExtension::class.java, JekyllExtension.NAME)
}
