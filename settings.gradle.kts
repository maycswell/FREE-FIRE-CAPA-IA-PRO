pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(
        RepositoriesMode.FAIL_ON_PROJECT_REPOS
    )
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "FREE-FIRE-CAPA-IA-PRO"
include(":app")
