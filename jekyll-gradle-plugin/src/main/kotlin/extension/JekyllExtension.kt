package dev.petuska.jekyll.extension

import dev.petuska.jekyll.extension.domain.JekyllCommonOptions
import dev.petuska.jekyll.extension.domain.JekyllSourceSets
import org.gradle.api.plugins.ExtensionAware

public abstract class JekyllExtension : JekyllCommonOptions, ExtensionAware {
  public companion object {
    public const val NAME: String = "jekyll"
  }

  @Suppress("UNCHECKED_CAST")
  public val sourceSets: JekyllSourceSets
    get() = extensions.getByName("sourceSets") as JekyllSourceSets
}
