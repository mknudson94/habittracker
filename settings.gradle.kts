pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("com.autonomousapps.build-health") version "3.18.0"

    // Optional, if using Kotlin
    id("org.jetbrains.kotlin.jvm") version "2.2.10" apply false

    // Optional, if using Android
    id("com.android.application") version "9.2.1" apply false
    id("com.android.library") version "9.2.1" apply false
    id("org.jetbrains.kotlin.android") version "2.2.10" apply false
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "HabitTracker"
include(":app")
include(":core:model")
include(":core:common")
include(":core:database")
include(":core:data")
include(":core:ui")
include(":core:nfc")
include(":feature:home")
include(":feature:auth")
include(":feature:add-habit")
include(":feature:habit-detail")
include(":feature:pair-nfc")
include(":feature:nfc-checkin")
