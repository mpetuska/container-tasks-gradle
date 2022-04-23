package dev.petuska.container.util

import dev.petuska.container.ContainerPlugin
import dev.petuska.container.task.ContainerExecTask.Mode
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider

internal class ContainerProject<E : ContainerPlugin.Extension>(
  project: Project,
  val extension: E,
  private val globalPrefix: String = "${extension.name}."
) : Project by project, PrefixedLogger by PrefixedLogger(extension.name, project.logger) {

  override fun getName(): String = project.name

  internal fun modeProvider(executable: String): Provider<Mode> = provider { Mode.detectOrNull(executable, this) }

  internal fun <T : Task> Property<T>.setFinal(provider: Provider<T>) {
    set(provider)
    finalizeValue()
  }

  fun <T> withExtension(action: E.() -> T) = with(extension, action)

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
