import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.DocsType
import java.io.File

plugins {
    `java-library`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

/**
 * Spring Boot version whose dependency graph we want offline
 */
val springBootVersion = "4.0.2"

/**
 * Offline Maven repo directory
 */
val offlineRepoDir: File =
    providers.gradleProperty("offlineRepoDir")
        .orElse("offline-repo")
        .map { File(rootDir, it).absoluteFile }
        .get()

/**
 * IMPORTANT:
 * Repositories are controlled from settings.gradle.kts
 * via dependencyResolutionManagement + repoMode
 */
repositories {
    // intentionally empty
}

/**
 * Dependencies to make available offline (full transitive closure)
 */
dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))
    implementation("org.springframework.boot:spring-boot-gradle-plugin:$springBootVersion")
    implementation("io.spring.gradle:dependency-management-plugin:1.1.6")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

/**
 * Copies an artifact into Maven repository layout:
 *
 * groupId/artifactId/version/
 *   artifactId-version[-classifier].ext
 */
fun copyToMavenRepo(
    moduleId: ModuleComponentIdentifier,
    artifactFile: File,
    classifier: String? = null,
    ext: String? = null
) {
    val groupPath = moduleId.group.replace('.', File.separatorChar)
    val artifact = moduleId.module
    val version = moduleId.version

    val targetDir = File(
        offlineRepoDir,
        "$groupPath${File.separator}$artifact${File.separator}$version"
    )
    targetDir.mkdirs()

    val effectiveExt = ext ?: artifactFile.extension
    val classifierSuffix = if (!classifier.isNullOrBlank()) "-$classifier" else ""
    val targetName = "$artifact-$version$classifierSuffix.$effectiveExt"

    artifactFile.copyTo(File(targetDir, targetName), overwrite = true)
}

tasks.register("bootstrapOfflineRepo") {

    doLast {

        val repoDir = file("offline-repo")
        repoDir.mkdirs()

        val configs = configurations.filter { it.isCanBeResolved }

        configs.forEach { config ->

            val resolved = config.resolvedConfiguration.resolvedArtifacts

            resolved.forEach { artifact ->

                val module = artifact.moduleVersion.id

                val groupPath = module.group.replace(".", "/")
                val artifactDir =
                    File(repoDir, "$groupPath/${artifact.name}/${module.version}")

                artifactDir.mkdirs()

                val jarFile =
                    File(artifactDir, "${artifact.name}-${module.version}.${artifact.extension}")

                artifact.file.copyTo(jarFile, overwrite = true)

                val pomSource =
                    artifact.file.parentFile
                        .listFiles()
                        ?.find { it.name.endsWith(".pom") }

                pomSource?.copyTo(
                    File(artifactDir, "${artifact.name}-${module.version}.pom"),
                    overwrite = true
                )
            }
        }

        println("Offline repo built at ${repoDir.absolutePath}")
    }
}

tasks.register("verifyOfflineRepo") {
    group = "offline"
    description = "Verifies offline repo contains JARs (Gradle-compatible check)"

    doLast {
        val jarCount = offlineRepoDir
            .walkTopDown()
            .count { it.isFile && it.extension == "jar" }

        println("JAR files: $jarCount")

        if (jarCount == 0) {
            error("Offline repo is incomplete (no JARs found)")
        }

        println("Offline repo verification PASSED (Gradle-compatible)")
    }
}
