import java.net.URL

plugins {
    java
}

val springBootVersion = "3.5.6"

repositories {
    mavenCentral()
    gradlePluginPortal()
}

val offline by configurations.creating

dependencies {
    // Spring runtime
    offline("org.springframework.boot:spring-boot-starter-web:$springBootVersion")
    offline("org.springframework.boot:spring-boot:$springBootVersion")
    offline("org.springframework.boot:spring-boot-autoconfigure:$springBootVersion")

    // Gradle plugin jar
    offline("org.springframework.boot:spring-boot-gradle-plugin:$springBootVersion")

    // Other libs
    offline("io.spring.gradle:dependency-management-plugin:1.1.6")
    offline("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    offline("org.apache.commons:commons-lang3:3.14.0")
}

tasks.register("buildOfflineRepo") {

    doLast {

        val repoDir = file("offline-repo")
        repoDir.mkdirs()

        // Resolve normal artifacts
        configurations["offline"].resolvedConfiguration.resolvedArtifacts.forEach { artifact ->

            val module = artifact.moduleVersion.id
            val group = module.group
            val artifactId = module.name
            val version = module.version

            val groupPath = group.replace(".", "/")

            val targetDir = File(repoDir, "$groupPath/$artifactId/$version")
            targetDir.mkdirs()

            val jarFile = File(targetDir, "$artifactId-$version.${artifact.extension}")
            artifact.file.copyTo(jarFile, overwrite = true)

            val pomUrl =
                "https://repo.maven.apache.org/maven2/$groupPath/$artifactId/$version/$artifactId-$version.pom"

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

        // Download Spring Boot plugin marker
        val markerPath =
            "org/springframework/boot/org.springframework.boot.gradle.plugin/$springBootVersion"

        val markerDir = File(repoDir, markerPath)
        markerDir.mkdirs()

        val markerPom =
            "https://repo.maven.apache.org/maven2/$markerPath/org.springframework.boot.gradle.plugin-$springBootVersion.pom"

        URL(markerPom).openStream().use { input ->
            File(markerDir, "org.springframework.boot.gradle.plugin-$springBootVersion.pom")
                .outputStream().use { output ->
                    input.copyTo(output)
                }
        }

        println("")
        println("Offline repo created at:")
        println(repoDir.absolutePath)
    }
}
