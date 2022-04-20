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
  named("jekyllMainInit", dev.petuska.jekyll.task.JekyllInitTask::class.java) {
    force.set(true)
  }
  val bundleMainExec = named("bundleMainExec", dev.petuska.jekyll.task.BundleExecTask::class.java)
  register("setupMain", dev.petuska.jekyll.task.BundleExecTask::class.java) {
    dependsOn("jekyllMainInit")
    workingDir.set(bundleMainExec.flatMap { it.workingDir })
    args.addAll("add", "webrick")
  }
}
