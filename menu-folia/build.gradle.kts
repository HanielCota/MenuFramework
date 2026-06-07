// menu-folia
//
// Folia scheduling strategy: re-renders run on the owning player's EntityScheduler instead of a
// (non-existent) global main thread. Implements the core MenuScheduler contract; the rest of the
// framework is unaware of the platform. The Folia API lives in the Paper API (compileOnly).

dependencies {
    "api"(project(":menu-core"))
    "compileOnly"("io.papermc.paper:paper-api:26.1.2.build.69-stable")
}
