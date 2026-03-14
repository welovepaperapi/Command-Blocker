pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://plugins.gradle.org/m2/")
    }
}
rootProject.name = "CmmandBlocker"

include("Core")
include("Paper")
include("Velocity")