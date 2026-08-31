pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // nitrite-bridge and dbinspect-bridge are not published yet; see ../pom.xml.
        mavenLocal()
    }
}

rootProject.name = "nitrite-bridge-example-android"
include(":app")
