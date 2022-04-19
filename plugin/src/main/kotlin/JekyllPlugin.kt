package dev.petuska.jekyll

import dev.petuska.jekyll.config.configure
import dev.petuska.jekyll.extension.JekyllExtension
import dev.petuska.jekyll.task.JekyllBuildTask
import dev.petuska.jekyll.util.ProjectEnhancer
import org.gradle.api.Plugin
import org.gradle.api.Project

public class JekyllPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    ProjectEnhancer(
      project = project,
      extension = project.extensions.create(JekyllExtension.NAME, JekyllExtension::class.java)
    ).apply()
  }

  private fun ProjectEnhancer.apply() {
//    configure(tasks)
    configure(extension)
    tasks.named("jekyllMainBuild", JekyllBuildTask::class.java).get().destination.asFile.orNull
      .let { println("FINAL BUILD >>>>>>>>>>>>> $it") }
//    tasks.named("jekyllMainInit", JekyllInitTask::class.java).get().destination.asFile.orNull
//      .let { println("FINAL >>>>>>>>>>>>> $it") }
  }
}
