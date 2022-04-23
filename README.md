[![Slack chat](https://img.shields.io/badge/kotlinlang-green?logo=slack&style=flat-square)](https://kotlinlang.slack.com/team/UL1A5BA2X)
[![Dokka docs](https://img.shields.io/badge/docs-dokka-orange?style=flat-square)](http://mpetuska.github.io/jekyll-gradle)
[![Version gradle-plugin-portal](https://img.shields.io/maven-metadata/v?label=gradle%20plugin%20portal&logo=gradle&metadataUrl=https%3A%2F%2Fplugins.gradle.org%2Fm2%2Fdev.petuska%2Fjekyll-gradle-plugin%2Fmaven-metadata.xml&style=flat-square)](https://plugins.gradle.org/plugin/dev.petuska.jekyll)
[![Version maven-central](https://img.shields.io/maven-central/v/dev.petuska/jekyll-gradle-plugin?logo=apache-maven&style=flat-square)](https://mvnrepository.com/artifact/dev.petuska/jekyll-gradle-plugin/latest)

# JEKYLL GRADLE PLUGIN

Gradle plugin providing easy configuration and tasks for managing your jekyll projects via gradle.
Supports running via native jekyll installations as well as podman or docker container runtimes.

> The plugin was last tested with `JDK 11` & `Gradle 7.3.3`

## Setup

```kotlin
plugins {
  id("dev.petuska.container.jekyll") version "<VERSION>"
}
```

When applied, the plugin automatically registers a single `main` jekyll sourceSet available at `./doc/main`.
To initialise it with a fresh jekyll project scaffold, run `./gradlew jekyllMainInit` task.
> If you are using Ruby version >3.0.0 or container mode, serve tasks may fail.
> You may fix it by adding `webrick` to your dependencies: ./gradlew bundleMainExec --arg=add --arg=webrick

From there, you can use `./gradlew jekyllMainServe --continuous` to start development server
and `./gradlew jekyllMainBuild` to produce a static website at `./build/jekyll/main`

### Configuration

You can your own custom jekyll sourceSets or extend existing ones with additional sources via the `jekyll` extension.

#### DSL

```kotlin
jekyll {
  mode // Explicitly set default jekyll execution mode and stop it from being automatically detected.
  version // Set docker.io/jekyll/jekyll image version to use for containerised executions
  environment // Modify the default environment variables for jekyll tasks
  sourceSets {
    main {
      mode // Explicitly set default jekyll execution mode for tasks linked to this sourceSet and stop it from being automatically detected.
      version // Set docker.io/jekyll/jekyll image version to use for containerised executions  linked to this sourceSet
      environment // Modify the default environment variables for jekyll tasks linked to this sourceSet
      jekyll // Add additional jekyll source roots to this sourceSet
      resources // Add additional jekyll resource roots to this sourceSet
    }
  }
}
```

## Tasks

The plugin generates the following gradle tasks for each sourceSet. All tasks wrap jekyll tasks with their options
exposed as gradle options. To learn more run `./gradlew help --task=<task>`

* `bundle<SourceSetName>Exec: BundleExecTask` - executes `bundle` commands on the sourceSet
* `jekyll<SourceSetName>Exec: JekyllExecTask` - executes `jekyll` commands on the sourceSet
* `jekyll<SourceSetName>Init: JekyllInitTask` - initialises the sourceSet with default jekyll scaffold
* `jekyll<SourceSetName>ProcessResources: ProcessResources` - processes sourceSet resources
  into `resources/jekyll/<sourceSet>`
* `jekyll<SourceSetName>Assemble: Copy` - assembles all sourceSet sources into `build/jekyll/<sourceSet>/sources`
* `jekyll<SourceSetName>Build: JekyllBuildTask` - builds the sourceSet into `build/jekyll/<sourceSet>/site`
* `jekyll<SourceSetName>Serve: JekyllServeTask` - assembles and serves the sourceSet
  from `build/jekyll/<sourceSet>/sources` (supports `--continuous` execution)

In addition to sourceSet tasks, two generic exec tasks are also provided for CLI usage:

* BundleExecTask: named as `bundleExec`
* JekyllExecTask: named as `jekyllExec`
