package dev.petuska.container.task.runner

import org.gradle.deployment.internal.Deployment
import org.gradle.deployment.internal.DeploymentHandle
import org.gradle.internal.service.ServiceRegistry

public interface AsyncRunner<EH : Any, ER : Any> {
  public fun execute(services: ServiceRegistry): ER

  public fun start(): EH

  public abstract class Handle<EH : Any> : DeploymentHandle {
    protected abstract val runner: AsyncRunner<EH, *>

    private var process: EH? = null

    final override fun isRunning(): Boolean = process != null

    final override fun start(deployment: Deployment) {
      process = runner.start()
    }

    final override fun stop() {
      val p = process
      if (p != null && abort(p)) {
        process = null
      }
    }

    public abstract fun abort(process: EH): Boolean
  }
}
