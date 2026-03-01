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

/**
 * MAIN TASK:
 * 1. Resolves all resolvable configurations (downloads everything)
 * 2. Copies JARs into offline-repo/
 * 3. Copies POMs via artifactView (DocsType = "pom")
 */
tasks.register("bootstrapOfflineRepo") {
    group = "offline"
    description = "Creates a portable Maven-style offline repository in ./offline-repo"

    doLast {
        println("Offline repo directory: ${offlineRepoDir.absolutePath}")
        offlineRepoDir.mkdirs()

        val resolvableConfs = configurations.filter { it.isCanBeResolved }

        // ------------------------------------------------------------
        // 1. Resolve everything (fills Gradle cache)
        // ------------------------------------------------------------
        println("Resolving configurations...")
        resolvableConfs.forEach { cfg ->
            println("  - ${cfg.name}")
            cfg.resolve()
        }

        // ------------------------------------------------------------
        // 2. Copy resolved JAR artifacts
        // ------------------------------------------------------------
        println("Copying JAR artifacts...")
        resolvableConfs.forEach { cfg ->
            val artifacts: Set<ResolvedArtifactResult> =
                cfg.incoming.artifacts.artifacts

            artifacts.forEach { art ->
                val id = art.id.componentIdentifier
                if (id is ModuleComponentIdentifier) {
                    copyToMavenRepo(
                        moduleId = id,
                        artifactFile = art.file
                    )
                }
            }
        }

        // ------------------------------------------------------------
        // 3. Resolve and copy POM files (THE ONLY SAFE WAY IN GRADLE 9)
        // ------------------------------------------------------------
        println("Resolving and copying POM files via artifact view...")

        val pomArtifacts = resolvableConfs.flatMap { cfg ->
            cfg.incoming.artifactView {
                attributes {
                    attribute(
                        Category.CATEGORY_ATTRIBUTE,
                        objects.named(Category::class.java, Category.DOCUMENTATION)
                    )
                    attribute(
                        DocsType.DOCS_TYPE_ATTRIBUTE,
                        objects.named(DocsType::class.java, "pom")
                    )
                }
            }.artifacts.artifacts
        }

        pomArtifacts.forEach { artifact ->
            val id = artifact.id.componentIdentifier
            if (id is ModuleComponentIdentifier) {
                copyToMavenRepo(
                    moduleId = id,
                    artifactFile = artifact.file,
                    ext = "pom"
                )
            }
        }

        println("Offline Maven repo successfully created at:")
        println("   ${offlineRepoDir.absolutePath}")
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
