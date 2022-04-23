plugins {
  id("dev.petuska.container.jekyll")
}

gradleEnterprise {
  buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"
  }
}

tasks {
  named("jekyllMainInit", dev.petuska.container.jekyll.task.JekyllInitTask::class.java) {
    force.set(true)
  }
  val bundleMainExec = named("bundleMainExec", dev.petuska.container.jekyll.task.BundleExecTask::class.java)
  register("setupMain", dev.petuska.container.jekyll.task.BundleExecTask::class.java) {
    dependsOn("jekyllMainInit")
    workingDir.set(bundleMainExec.flatMap { it.workingDir })
    args.addAll("add", "webrick")
  }
}
