package dev.petuska.jekyll.extension.domain

import org.gradle.api.tasks.Internal

public enum class JekyllMode {
  NATIVE, PODMAN, DOCKER
}

@Suppress("PropertyName", "VariableNaming")
public interface JekyllModeScope {
  @get:Internal
  public val NATIVE: JekyllMode get() = JekyllMode.NATIVE

  @get:Internal
  public val PODMAN: JekyllMode get() = JekyllMode.PODMAN

  @get:Internal
  public val DOCKER: JekyllMode get() = JekyllMode.DOCKER
}
