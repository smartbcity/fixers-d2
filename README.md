# D2

D2 is a module for generating automatic documentation of the SmartB architecture

## dokka-storybook-plugin

Plugin [Dokka](https://github.com/Kotlin/dokka) compatible with Storybook and [Archetypes-UI](https://gitlab.smartb.city/app/archetypes-ui)

### Getting Started

#### Create a Gradle task

Dokka can be started from inside a gradle task. 

First, import it in your project:

*build.gradle.kts at root level*
```kotlin
plugins {
    id("org.jetbrains.dokka") version "1.4.32"
}
```

Then create the task within the module you wish to generate documentation for:

> NB: Multi-module generation is not supported ATM

*build.gradle.kts of the module*
```kotlin
tasks {
    create<org.jetbrains.dokka.gradle.DokkaTask>("dokkaD2") {
        dependencies {
            plugins(project(":dokka-storybook-plugin"))
        }
    }
}
```

#### Write documentation

This module follows the same syntax as the official [KDoc](https://kotlinlang.org/docs/kotlin-doc.html) with a few extra tags.

> NB: Documentation generation is currently supported for Interface and Typealias nodes.
> Any other object might generate either unstable results or no result at all. 

Additional KDoc tags (case insensitive):
- @d2 --> class-level, mark an object as documentable
- @parent [reference.to.documentable.object] --> class-level, the object tagged will be incorporated to its parent documentation page
- @example "jsonValue" --> property-level, value to fill the json with
