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
    implementation("org.springframework.boot:spring-boot-gradle-plugin:$springBootVersion")
    implementation("io.spring.gradle:dependency-management-plugin:1.1.6")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    implementation("org.apache.commons:commons-lang3:3.14.0")
}

tasks.register("buildOfflineRepo") {
    doLast {
        val repoDir = file("offline-repo")
        repoDir.mkdirs()
        configurations.filter { it.isCanBeResolved }.forEach { config ->
            config.resolvedConfiguration.resolvedArtifacts.forEach { artifact ->
                val module = artifact.moduleVersion.id
                val groupPath = module.group.replace(".", "/")
                val targetDir = File(repoDir, "$groupPath/${artifact.name}/${module.version}")
                targetDir.mkdirs()
                val jarFile = File(targetDir, "${artifact.name}-${module.version}.${artifact.extension}")
                artifact.file.copyTo(jarFile, overwrite = true)
                val pomUrl = "https://repo.maven.apache.org/maven2/$groupPath/${artifact.name}/${module.version}/${artifact.name}-${module.version}.pom"
                val pomFile = File(targetDir, "${artifact.name}-${module.version}.pom")
                try {
                    URL(pomUrl).openStream().use { input ->
                        pomFile.outputStream().use { output -> input.copyTo(output) }
                    }
                } catch (e: Exception) {
                    println("POM not found for ${artifact.name}")
                }
                println("Added ${artifact.name}:${module.version}")
            }
        }
        println("Offline repo created at: ${repoDir.absolutePath}")
    }
}
