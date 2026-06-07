// example-plugin
//
// Demonstration plugin consuming the framework. Shades and relocates Configurate (and its
// geantyref dependency) so the runtime jar is self-contained and cannot clash with another
// plugin's copy. Not published.

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.gradleup.shadow") version "9.4.2"
}

dependencies {
    "implementation"(project(":menu-core"))
    "implementation"(project(":menu-paper"))
    "implementation"(project(":menu-folia"))
    "compileOnly"("io.papermc.paper:paper-api:26.1.2.build.69-stable")

    // paper-api is compileOnly (never transitive), so the test source set needs its own copy to
    // reference Bukkit types; Mockito mocks the final services and Bukkit interfaces server-free.
    "testCompileOnly"("io.papermc.paper:paper-api:26.1.2.build.69-stable")
    "testRuntimeOnly"("io.papermc.paper:paper-api:26.1.2.build.69-stable")
    "testImplementation"("org.mockito:mockito-core:5.23.0")
}

tasks.withType<ShadowJar>().configureEach {
    archiveClassifier.set("")
    relocate("org.spongepowered.configurate", "dev.haniel.menu.libs.configurate")
    relocate("io.leangen.geantyref", "dev.haniel.menu.libs.geantyref")
    relocate("com.github.benmanes.caffeine", "dev.haniel.menu.libs.caffeine")
    relocate("io.github.classgraph", "dev.haniel.menu.libs.classgraph")
    relocate("nonapi.io.github.classgraph", "dev.haniel.menu.libs.nonapi.classgraph")
}

tasks.named("build") {
    dependsOn("shadowJar")
}
