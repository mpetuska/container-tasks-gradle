package dev.petuska.jekyll.extension

import dev.petuska.jekyll.extension.domain.JekyllCommonOptions
import org.gradle.api.plugins.ExtensionAware

public abstract class JekyllExtension : JekyllCommonOptions, ExtensionAware {
  public companion object {
    public const val NAME: String = "jekyll"
  }
}
