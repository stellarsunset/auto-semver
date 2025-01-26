# AutoSemver

[![Test](https://github.com/stellarsunset/auto-semver/actions/workflows/test.yaml/badge.svg)](https://github.com/stellarsunset/auto-semver/actions/workflows/test.yaml)
[![codecov](https://codecov.io/gh/stellarsunset/auto-semver/graph/badge.svg?token=vOMUPNbOEG)](https://codecov.io/gh/stellarsunset/auto-semver)

Automatic semantic version generation for Gradle projects based on annotated git tags.

## Motivation

There are a variety of these repositories out there with exposing configurability for a large swath of different semver
formats and styles.

However, writing one of these plugins isn't challenging, and it allows you to:

1. Minimize the required configuration across projects
2. Bake in exactly the versioning semantics you want

This plugin generates only one additional task, and requires zero additional configuration.

## Usage

Add the plugin to your build from the Gradle plugin portal.

```kotlin
plugins {
    id('io.github.stellarsunset.auto-semver') version "0.0.1"
}
```

This plugin adds a transparent Gradle task that will dynamically compute the runtime version of the project based on the
last [annotated Git tag](https://git-scm.com/book/en/v2/Git-Basics-Tagging) in the repo.

```bash
# To create a new release you can manually commit an annotated tag 
$ git tag -a v1.4.1 -m "Tag release 1.4.1"

# Subsequent builds will read this tag to determine project version
$ ./gradlew build
```

For convenience, it also exposes a task that will auto-increment and commit a new annotated tag to the repo based on the
last annotated tag and the desired version component to increment.

```bash
# Commit a new annotated tag on the current commit with an incremented major release 
# version and default commit message
$ ./gradlew release -Pmajor

# Add a custom commit message if you want...
$ ./gradlew release -Pmajor -Pmessage "Some custom commit message..."
```

To register a custom task that will show the inferred version

```kotlin
// The plugin sets the project.version when it's applied, and therefore the correct 
// version is available for reference in all build.gradle.kts configured tasks
tasks.register("showVersion") {
    inputs.property("version", project.version)
    doLast {
        println("Project Version: ${inputs.properties["version"]}")
    }
}
```