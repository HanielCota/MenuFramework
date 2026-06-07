import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication

plugins {
    base
    id("com.diffplug.spotless") version "7.2.1" apply false
}

val targetJavaVersion = 25

// Library modules consumed by other plugins (published; example-plugin is a demo, not published).
val publishedModules = setOf("menu-core", "menu-paper", "menu-folia")

allprojects {
    group = "dev.haniel.menu"
    version = "0.1.0"
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
        if (name in publishedModules) {
            withSourcesJar()
            withJavadocJar()
        }
    }

    if (name in publishedModules) {
        apply(plugin = "maven-publish")
        // The published Javadoc jar should build quietly; doclint flags compact-constructor records
        // that intentionally carry their docs on the record header, not the canonical constructor.
        tasks.withType<Javadoc>().configureEach {
            (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
        }
        // JitPack builds straight from the Git tag and consumes the local Maven publication; the
        // GAV is dev.haniel.menu:<module>:<version>, exposed to consumers as
        // com.github.<user>.MenuFramework:<module>:<tag>.
        extensions.configure<PublishingExtension>("publishing") {
            publications {
                create("maven", MavenPublication::class.java) {
                    from(components["java"])
                }
            }
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
