import dev.petuska.jekyll.task.BundleExecTask
import dev.petuska.jekyll.task.JekyllInitTask

plugins {
  id("dev.petuska.jekyll")
}

gradleEnterprise {
  buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"
  }
}

tasks {
  named("jekyllMainInit", JekyllInitTask::class.java) {
    force.set(true)
  }
  val bundleMainExec = named("bundleMainExec", BundleExecTask::class.java)
  register("setupMain", BundleExecTask::class.java) {
    dependsOn("jekyllMainInit")
    workingDir.set(bundleMainExec.flatMap { it.workingDir })
    args.addAll("add", "webrick")
  }
}
