import org.gradle.api.plugins.JavaPluginExtension

plugins {
    base
    id("com.diffplug.spotless") version "7.2.1" apply false
}

val targetJavaVersion = 25

allprojects {
    group = "dev.haniel.menu"
    version = "0.1.0-SNAPSHOT"
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "jacoco")
    apply(plugin = "com.diffplug.spotless")

    extensions.configure<com.diffplug.gradle.spotless.SpotlessExtension>("spotless") {
        // The formatter is the source of truth: spotlessCheck runs as part of `check`/`build`.
        // Run `./gradlew spotlessApply` before committing to normalise formatting.
        java {
            googleJavaFormat("1.28.0")
            removeUnusedImports()
            trimTrailingWhitespace()
            endWithNewline()
        }
    }

    extensions.configure<JacocoPluginExtension>("jacoco") {
        toolVersion = "0.8.13"
    }

    tasks.withType<Test>().configureEach {
        finalizedBy(tasks.named("jacocoTestReport"))
    }

    tasks.withType<JacocoReport>().configureEach {
        reports {
            csv.required.set(true)
        }
    }

    extensions.configure<JavaPluginExtension>("java") {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
            vendor.set(JvmVendorSpec.AZUL)
        }
    }

    dependencies {
        "testImplementation"(platform("org.junit:junit-bom:6.0.0"))
        "testImplementation"("org.junit.jupiter:junit-jupiter")
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
    }

    configurations.all {
        resolutionStrategy {
            // Pin transitive dependencies of the compile-only Paper API away from known CVEs
            // (CVE-2025-48924 in commons-lang3, CVE-2025-67030 in plexus-utils). They arrive via
            // paper-api -> maven-resolver-provider and never reach a shipped artifact (the server
            // provides its own classpath at runtime), so this only silences dependency scanners.
            force(
                "org.apache.commons:commons-lang3:3.18.0",
                "org.codehaus.plexus:plexus-utils:4.0.2",
            )
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(targetJavaVersion)
        options.compilerArgs.addAll(listOf("-Xlint:deprecation", "-Xlint:unchecked"))
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }
}
