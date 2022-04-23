plugins {
  id("plugin.common")
  id("plugin.publishing")
}

kotlin {
  explicitApi()
  dependencies {
    testImplementation("io.kotest:kotest-assertions-core:_")
    testImplementation("io.kotest:kotest-assertions-json:_")
    testImplementation("org.junit.jupiter:junit-jupiter-api:_")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:_")
  }
}

gradlePlugin {
  plugins {
    create("jekyll") {
      id = "dev.petuska.container.jekyll"
      displayName = "Jekyll Gradle Plugin"
      description = "A gradle task wrapper over jekyll cli allowing executions via native or containerised executables"
      implementationClass = "dev.petuska.container.jekyll.JekyllPlugin"
    }
  }
}

pluginBundle {
  website = "https://github.com/mpetuska/${rootProject.name}"
  vcsUrl = "https://github.com/mpetuska/${rootProject.name}.git"
  tags = listOf("container", "executable")
  description = project.description
}
