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
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "BaseProject"
include(":app")
include(":features:billing")
include(":features:ads")
include(":features:common")
project(":features:billing").projectDir = File(settingsDir, "features/billing")
project(":features:ads").projectDir = File(settingsDir, "features/ads")
project(":features:common").projectDir = File(settingsDir, "features/common")
