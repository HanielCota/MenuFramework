pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

// Auto-provisions the Java 25 toolchain on CI and JitPack, where the JDK may be absent.
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.extendedclip.com/releases/") // PlaceholderAPI (soft dependency)
    }
}

rootProject.name = "MenuFramework"

include(":menu-core")
include(":menu-paper")
include(":menu-folia")
include(":example-plugin")
