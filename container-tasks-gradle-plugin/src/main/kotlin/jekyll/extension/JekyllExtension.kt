package dev.petuska.container.jekyll.extension

import dev.petuska.container.ContainerPlugin.Extension
import dev.petuska.container.jekyll.extension.domain.JekyllSourceSets

public abstract class JekyllExtension : Extension {
  public companion object {
    public const val NAME: String = "jekyll"
  }

  @Suppress("UNCHECKED_CAST")
  public val sourceSets: JekyllSourceSets
    get() = extensions.getByName("sourceSets") as JekyllSourceSets
}
