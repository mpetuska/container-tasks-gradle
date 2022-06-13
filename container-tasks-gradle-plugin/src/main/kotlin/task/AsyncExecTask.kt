package dev.petuska.container.task

import dev.petuska.container.task.runner.AsyncRunner
import dev.petuska.container.util.PrefixedLogger
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.deployment.internal.DeploymentRegistry
import kotlin.reflect.KClass

@Suppress("LeakingThis", "TooManyFunctions")
public abstract class AsyncExecTask<H : Any, T : AsyncRunner<H, *>>(
  private val handleType: KClass<out AsyncRunner.Handle<H>>,
  logMarker: String = "async-exec",
) : DefaultTask() {
  private val _logger: PrefixedLogger = PrefixedLogger(logMarker, super.getLogger())

  @Internal
  public override fun getLogger(): PrefixedLogger = _logger

  @get:Input
  public abstract val changeBehavior: Property<DeploymentRegistry.ChangeBehavior>

  @get:Input
  public abstract val async: Property<Boolean>

  init {
    changeBehavior.convention(DeploymentRegistry.ChangeBehavior.BLOCK)
    async.convention(project.gradle.startParameter.isContinuous)
  }

  protected fun <T> Property<T>.setFinal(value: T) {
    set(value)
    finalizeValue()
  }

  protected abstract fun buildRunner(): T

  protected open fun beforeAction(): Unit = Unit

  protected open fun afterAction(): Unit = Unit

  private val deploymentRegistry = services.get(DeploymentRegistry::class.java)

  @get:Internal
  protected val handle: AsyncRunner.Handle<H>? get() = deploymentRegistry.get(name, handleType.java)

  private fun execute() {
    val runner = buildRunner()
    if (async.get()) {
      if (handle == null) {
        deploymentRegistry.start(
          name,
          changeBehavior.get(),
          handleType.java,
          runner
        )
      }
    } else {
      runner.execute(services)
    }
  }

  @TaskAction
  @Suppress("UnusedPrivateMember")
  private fun action() {
    beforeAction()
    execute()
    afterAction()
  }
}
