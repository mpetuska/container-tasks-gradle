package dev.petuska.container.jekyll

import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test

/**
 * A simple unit test for the 'dev.petuska.jekyll.greeting' plugin.
 */
class JekyllPluginTest {
  @Test
  fun `plugin registers task`() {
    // Create a test project and apply the plugin
    val project = ProjectBuilder.builder().build()
    project.plugins.apply("dev.petuska.jekyll")

    // Verify the result
//    project.tasks.findByName("greeting").shouldNotBeNull()
  }
}
