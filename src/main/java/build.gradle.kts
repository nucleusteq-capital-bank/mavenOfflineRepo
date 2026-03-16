import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// ----------------------------
// VERSION VARIABLES
// ----------------------------
val springBootVersion = "3.5.6"
val dependencyManagementVersion = "1.1.6"
val javaVersion = "21"

// ----------------------------
// BUILDSCRIPT (PLUGIN LOADING)
// ----------------------------
buildscript {

    val springBootVersion: String by extra
    val dependencyManagementVersion: String by extra

    repositories {
        maven {
            url = uri("$rootDir/offline-repo")
        }
    }

    dependencies {
        classpath("org.springframework.boot:spring-boot-gradle-plugin:$springBootVersion")
        classpath("io.spring.gradle:dependency-management-plugin:$dependencyManagementVersion")
    }
}

// ----------------------------
// APPLY PLUGINS
// ----------------------------
apply(plugin = "java")
apply(plugin = "org.springframework.boot")
apply(plugin = "io.spring.dependency-management")

// ----------------------------
// PROJECT METADATA
// ----------------------------
group = "com.example"
version = "1.0.0"

// ----------------------------
// JAVA CONFIG
// ----------------------------
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(javaVersion))
    }
}

// ----------------------------
// REPOSITORIES
// ----------------------------
repositories {
    maven {
        url = uri("$rootDir/offline-repo")
    }
}

// ----------------------------
// DEPENDENCY VERSIONS
// ----------------------------
val lombokVersion = "1.18.32"
val junitVersion = "5.10.2"

// ----------------------------
// DEPENDENCIES
// ----------------------------
dependencies {

    implementation("org.springframework.boot:spring-boot-starter-web:$springBootVersion")

    compileOnly("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")

    testImplementation("org.springframework.boot:spring-boot-starter-test:$springBootVersion")
    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
}

// ----------------------------
// TEST CONFIG
// ----------------------------
tasks.test {
    useJUnitPlatform()
}

// ----------------------------
// JAR CONFIG
// ----------------------------
tasks.bootJar {
    archiveFileName.set("app.jar")
}
