plugins {
  id("dev.petuska.jekyll")
}

gradleEnterprise {
  buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"
  }
}

jekyll {
  sourceSets {
    main {
      println(sources.srcDirs.map { it.path })
    }
  }
}
