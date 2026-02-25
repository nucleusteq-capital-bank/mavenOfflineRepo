plugins {
    java
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    // TEMPORARILY keep this while refreshing dependencies
    mavenCentral()

    // Offline repo (kept for later)
    maven {
        setUrl(uri("${rootDir}/offline-repo"))
    }
}

dependencies {

    // =========================
    // Spring Boot BOM (CRITICAL)
    // =========================
    implementation(platform("org.springframework.boot:spring-boot-dependencies:3.5.6"))

    // =========================
    // Spring Boot Starters
    // =========================
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // =========================
    // MSSQL JDBC
    // =========================
    runtimeOnly("com.microsoft.sqlserver:mssql-jdbc:13.2.1.jre11")

    // =========================
    // Lombok (NOT managed by Boot BOM)
    // =========================
    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")

    testCompileOnly("org.projectlombok:lombok:1.18.34")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.34")

    // =========================
    // Okta
    // =========================
    implementation("com.okta.spring:okta-spring-boot-starter:3.0.6")

    // =========================
    // OpenAPI
    // =========================
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.13")

    // =========================
    // Flyway
    // =========================
    implementation("org.flywaydb:flyway-core:11.15.0")

    // =========================
    // Commons CSV
    // =========================
    implementation("org.apache.commons:commons-csv:1.14.1")

    // =========================
    // Testing
    // =========================
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }

    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.testcontainers:mssqlserver:1.21.3")
}

tasks.test {
    useJUnitPlatform()
}
