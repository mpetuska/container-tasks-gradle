plugins {
  id("de.fayard.refreshVersions") version "0.40.1"
  id("com.gradle.enterprise") version "3.8.1"
}

rootProject.name = "container-tasks-gradle"
include("container-tasks-gradle-plugin")
