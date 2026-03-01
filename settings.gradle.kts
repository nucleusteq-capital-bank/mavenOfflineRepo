// settings.gradle.kts
import java.io.File

pluginManagement {
    repositories {
        val repoMode =
            providers.gradleProperty("repoMode").orElse("bootstrap").get()

        val offlineRepoDir =
            providers.gradleProperty("offlineRepoDir")
                .orElse("offline-repo")
                .map { File(rootDir, it).absoluteFile }
                .get()

        if (repoMode == "consume") {
            maven { url = uri(offlineRepoDir) }
        } else {
            gradlePluginPortal()
            mavenCentral()
        }
    }
}

dependencyResolutionManagement {
    repositories {
        val repoMode =
            providers.gradleProperty("repoMode").orElse("bootstrap").get()

        val offlineRepoDir =
            providers.gradleProperty("offlineRepoDir")
                .orElse("offline-repo")
                .map { File(rootDir, it).absoluteFile }
                .get()

        if (repoMode == "consume") {
            maven { url = uri(offlineRepoDir) }
        } else {
            mavenCentral()
        }
    }
}

rootProject.name = "offline-repo-bootstrap"
