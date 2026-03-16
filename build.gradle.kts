import java.net.URL

plugins {
    java
}

val springBootVersion = "3.5.6"

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web:$springBootVersion")
    implementation("org.springframework.boot:spring-boot:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-autoconfigure:$springBootVersion")

    // Gradle plugin artifacts (required for offline builds)
    implementation("org.springframework.boot:spring-boot-gradle-plugin:$springBootVersion")
    implementation("org.springframework.boot:org.springframework.boot.gradle.plugin:$springBootVersion")

    // Spring dependency management plugin
    implementation("io.spring.gradle:dependency-management-plugin:1.1.6")

    // Other libraries
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    implementation("org.apache.commons:commons-lang3:3.14.0")
}

tasks.register("buildOfflineRepo") {
    doLast {
        val repoDir = file("offline-repo")
        repoDir.mkdirs()

        val configs = listOf(
            configurations.compileClasspath.get(),
            configurations.runtimeClasspath.get()
        )

        configs.forEach { config ->
            config.resolvedConfiguration.resolvedArtifacts.forEach { artifact ->
                val module = artifact.moduleVersion.id
                val group = module.group
                val artifactId = module.name
                val version = module.version
                val groupPath = group.replace(".", "/")

                val targetDir = File(repoDir, "$groupPath/$artifactId/$version")
                targetDir.mkdirs()

                val jarFile = File(targetDir, "$artifactId-$version.${artifact.extension}")
                artifact.file.copyTo(jarFile, overwrite = true)

                val pomUrl = "https://repo.maven.apache.org/maven2/$groupPath/$artifactId/$version/$artifactId-$version.pom"
                val pomFile = File(targetDir, "$artifactId-$version.pom")

                if (!pomFile.exists()) {
                    try {
                        URL(pomUrl).openStream().use { input ->
                            pomFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                    } catch (e: Exception) {
                        println("Missing POM for $group:$artifactId:$version")
                    }
                }

                println("Added $group:$artifactId:$version")
            }
        }

        println("Offline repo created at: ${repoDir.absolutePath}")
    }
}
