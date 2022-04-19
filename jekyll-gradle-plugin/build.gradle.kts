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
