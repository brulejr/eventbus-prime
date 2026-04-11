val ksbCommonsVersion: String by project
val projectVersion: String by project

val useLocalKsbCommons: Boolean =
    providers.gradleProperty("useLocalKsbCommons")
        .map { it.toBoolean() }
        .orElse(true)
        .get()

plugins {
    kotlin("jvm") version "2.2.10"
    kotlin("plugin.spring") version "2.2.10"
    id("org.springframework.boot") version "3.5.8"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "io.jrb.labs"
version = "0.0.1-SNAPSHOT"

/**
 * Resolve the project version:
 *
 * - In CI on a tag build, GITHUB_REF_NAME will be something like "v0.3.1".
 *   We strip the leading "v" and use "0.3.1" as the version.
 * - Otherwise, fall back to projectVersion from gradle.properties.
 */
version = System.getenv("GITHUB_REF_NAME")
    ?.let { refName ->
        if (refName.matches(Regex("""v\d+\.\d+\.\d+"""))) {
            refName.removePrefix("v")
        } else {
            projectVersion
        }
    }
    ?: projectVersion

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
    if (!useLocalKsbCommons) {
        maven {
            url = uri("https://maven.pkg.github.com/brulejr/ksb-commons")
            credentials {
                username = findProperty("gpr.user") as String?
                    ?: System.getenv("GITHUB_PACKAGES_USER")
                            ?: System.getenv("GITHUB_ACTOR")
                            ?: "brulejr"
                password = findProperty("gpr.key") as String?
                    ?: System.getenv("GITHUB_PACKAGES_TOKEN")
                            ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

dependencies {
    if (useLocalKsbCommons) {
        implementation(platform("io.jrb.labs:ksb-dependency-bom"))
    } else {
        implementation(platform("io.jrb.labs:ksb-dependency-bom:$ksbCommonsVersion"))
    }
    implementation("io.jrb.labs:ksb-commons-ms-core")
    implementation("io.jrb.labs:ksb-commons-workflow-core")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.assertj:assertj-core")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(libs.mockk)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

springBoot {
    // info.app.version will now be aligned with tag-derived project.version
    buildInfo()
}
