plugins {
    `java-library`
    `maven-publish`
    id("com.diffplug.spotless") version "8.7.0"
}

group = providers.gradleProperty("group").get()
version = providers.gradleProperty("version").get()

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
    val examples by creating {
        java.srcDir("examples/src/main/java")
        resources.srcDir("examples/src/main/resources")
        compileClasspath += main.output + main.compileClasspath
        runtimeClasspath += output + compileClasspath
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
    withSourcesJar()
    withJavadocJar()
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

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
