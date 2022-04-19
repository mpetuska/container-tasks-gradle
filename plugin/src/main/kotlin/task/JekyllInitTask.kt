package dev.petuska.jekyll.task

import dev.petuska.jekyll.extension.domain.JekyllMode

@Suppress("LeakingThis")
public abstract class JekyllInitTask : JekyllExecTask() {
  init {
    description = "Initialises jekyll website"
    outputs.upToDateWhen { false }
    destination.convention(workingDir)
  }

  override val command: String = "new"

  override fun prepareJekyllArgs(mode: JekyllMode): List<String> {
    val args = mutableListOf<String>(".")
    return super.prepareJekyllArgs(mode) + args
  }
}
