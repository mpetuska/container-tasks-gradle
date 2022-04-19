package dev.petuska.jekyll.extension.domain

import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property

public interface JekyllCommonOptions {
  public val mode: Property<JekyllMode>
  public val version: Property<String>
  public val environment: MapProperty<String, Any>
}
