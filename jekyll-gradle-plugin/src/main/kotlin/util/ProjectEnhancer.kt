package dev.petuska.jekyll.util

import dev.petuska.jekyll.extension.JekyllExtension
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider

internal class ProjectEnhancer(
  project: Project,
  val extension: JekyllExtension = project.extensions.findByType(JekyllExtension::class.java)
    ?: error("JekyllExtension not found"),
  private val globalPrefix: String = "jekyll."
) : Project by project, PluginLogger {

  fun <T> withExtension(action: JekyllExtension.() -> T) = with(extension, action)

  /**
   * Convention resolution order by descending priority
   * 1. CLI arguments (`--arg=value`)
   * 2. System properties (`-Dprop=value`)
   * 3. Gradle properties (`-Pprop=value`,
   *    `ORG_GRADLE_PROJECT_prop=value` env variable,
   *    `-Dorg.gradle.project.prop=value` system property
   *    or `prop=value` stored in `gradle.properties` file)
   * 4. Env variables (`PROP=value`)
   * 5. [default] value provider
   *
   * Additionally, prop names replace spaces with dots.
   * Env variable names are uppercase prop names with dots replaced with underscores.
   */
  fun <T : Any, P : Property<T>> P.sysProjectEnvPropertyConvention(
    name: String,
    default: Provider<T> = providers.provider { null },
    converter: (String) -> T
  ) {
    val propName = globalPrefix + name
    val envName = name.uppercase().replace(".", "_")

    convention(
      providers.systemProperty(propName)
        .orElse(provider { extensions.extraProperties.properties[propName]?.toString() })
        .orElse(providers.gradleProperty(propName))
        .orElse(providers.environmentVariable(envName))
        .map(converter)
        .orElse(default)
    )
  }

  fun <P : Property<String>> P.sysProjectEnvPropertyConvention(
    name: String,
    default: Provider<String> = providers.provider { null },
  ) = sysProjectEnvPropertyConvention(name, default) { it }
}
