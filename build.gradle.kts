import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SourcesJar
import org.gradle.plugins.signing.Sign

plugins {
    `java-library`
    `maven-publish`
    id("com.diffplug.spotless") version "8.7.0"
    id("com.vanniktech.maven.publish") version "0.37.0"
}

group = providers.gradleProperty("group").get()
version = providers.gradleProperty("version").get()

val artifactId = providers.gradleProperty("artifactId").get()
val paperApiVersion = providers.gradleProperty("paperApiVersion").get()
val mockBukkitVersion = providers.gradleProperty("mockBukkitVersion").get()
val junitVersion = providers.gradleProperty("junitVersion").get()
val googleJavaFormatVersion = providers.gradleProperty("googleJavaFormatVersion").get()
val jspecifyVersion = providers.gradleProperty("jspecifyVersion").get()

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnlyApi("org.jspecify:jspecify:$jspecifyVersion")
    compileOnly("io.papermc.paper:paper-api:$paperApiVersion")
    testImplementation("io.papermc.paper:paper-api:$paperApiVersion")

    testImplementation(platform("org.junit:junit-bom:$junitVersion"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(
        "org.mockbukkit.mockbukkit:mockbukkit-v26.1.2:$mockBukkitVersion"
    )
}

sourceSets {
    val main by getting
    val test by getting
    val examples by creating {
        java.srcDir("examples/src/main/java")
        resources.srcDir("examples/src/main/resources")
        compileClasspath += main.output + main.compileClasspath
        runtimeClasspath += output + compileClasspath
    }
    val examplesTest by creating {
        java.srcDir("examples/src/test/java")
        resources.srcDir("examples/src/test/resources")
        compileClasspath += main.output + examples.output + test.output
        compileClasspath += main.compileClasspath + test.compileClasspath
        runtimeClasspath += output + compileClasspath + test.runtimeClasspath
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

spotless {
    java {
        target("src/**/*.java", "examples/**/*.java")
        googleJavaFormat(googleJavaFormatVersion)
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }

    format("projectFiles") {
        target(
            "*.gradle.kts",
            "*.properties",
            "*.md",
            "examples/**/*.md",
            "examples/**/*.yml",
            "src/**/*.properties"
        )
        trimTrailingWhitespace()
        endWithNewline()
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.named<JavaCompile>("compileJava") {
    options.release.set(25)
    options.compilerArgs.addAll(listOf("-Xlint:all", "-Werror"))
}

tasks.named<JavaCompile>("compileTestJava") {
    options.release.set(25)
    options.compilerArgs.add("-Xlint:all")
}

tasks.named<JavaCompile>("compileExamplesJava") {
    mustRunAfter("spotlessApply")
    options.release.set(25)
    options.compilerArgs.addAll(listOf("-Xlint:all", "-Werror"))
}

tasks.named<JavaCompile>("compileExamplesTestJava") {
    mustRunAfter("spotlessApply")
    options.release.set(25)
    options.compilerArgs.add("-Xlint:all")
}

tasks.register<Test>("examplesTest") {
    description = "Runs tests for the examples source set."
    group = "verification"
    testClassesDirs = sourceSets["examplesTest"].output.classesDirs
    classpath = sourceSets["examplesTest"].runtimeClasspath
    useJUnitPlatform()
}

tasks.named("check") {
    dependsOn("examplesTest")
}

tasks.withType<Javadoc>().configureEach {
    exclude("**/internal/**")
    options.encoding = "UTF-8"
    (options as StandardJavadocDocletOptions).apply {
        addBooleanOption("Xdoclint:all,-missing", true)
        addBooleanOption("quiet", true)
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.named("check") {
    dependsOn("examplesClasses")
    dependsOn("spotlessApply")
    dependsOn("spotlessCheck")
}

tasks.named("spotlessCheck") {
    mustRunAfter("spotlessApply")
}

tasks.register("format") {
    group = "formatting"
    description = "Applies Google Java Format and the configured project formatting rules."
    dependsOn("spotlessApply")
}

tasks.withType<Sign>().configureEach {
    onlyIf {
        !gradle.startParameter.taskNames.any { it.contains("MavenLocal", ignoreCase = true) }
    }
}

mavenPublishing {
    configure(
        JavaLibrary(
            javadocJar = JavadocJar.Javadoc(),
            sourcesJar = SourcesJar.Sources()
        )
    )

    coordinates(
        groupId = group.toString(),
        artifactId = artifactId,
        version = version.toString()
    )

    publishToMavenCentral()
    signAllPublications()

    pom {
        name.set("MenuFramework")
        description.set(
            "A type-safe inventory menu framework for Paper and Folia plugins."
        )
        inceptionYear.set("2026")
        url.set("https://github.com/HanielCota/MenuFramework")

        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/license/mit")
                distribution.set("repo")
            }
        }

        developers {
            developer {
                id.set("HanielCota")
                name.set("Haniel Fialho")
                url.set("https://github.com/HanielCota")
            }
        }

        scm {
            url.set("https://github.com/HanielCota/MenuFramework")
            connection.set("scm:git:https://github.com/HanielCota/MenuFramework.git")
            developerConnection.set("scm:git:ssh://git@github.com/HanielCota/MenuFramework.git")
        }
    }
}
