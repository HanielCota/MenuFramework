// menu-paper
//
// Paper adapter layer: the only framework module that sees the Paper API, and it sees it as
// compileOnly (the server provides it at runtime). Caffeine backs the rendered-page cache.

dependencies {
    "api"(project(":menu-core"))
    "implementation"(project(":menu-folia"))
    "compileOnly"("io.papermc.paper:paper-api:26.1.2.build.69-stable")
    // PlaceholderAPI is an optional runtime soft dependency; touched only via the isolated Papi class.
    "compileOnly"("me.clip:placeholderapi:2.11.6")
    "testCompileOnly"("io.papermc.paper:paper-api:26.1.2.build.69-stable")
    "testRuntimeOnly"("io.papermc.paper:paper-api:26.1.2.build.69-stable")
    "testImplementation"("org.mockito:mockito-core:5.23.0")
    "implementation"("com.github.ben-manes.caffeine:caffeine:3.2.4")

    // Boot-time @Menu discovery. ClassGraph scans the classpath once at startup; it is never
    // touched at runtime. Lives here (the integration layer) rather than a separate module.
    "implementation"("io.github.classgraph:classgraph:4.8.184")
}
