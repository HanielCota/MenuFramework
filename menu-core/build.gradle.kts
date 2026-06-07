// menu-core
//
// Pure-Java heart of the framework: annotations (behaviour), the immutable config model
// (appearance), the merge step and the compiler. No Bukkit/Paper types so the domain stays
// testable without a server. Configurate is exposed as `api` because the public config
// records are annotated with it.

dependencies {
    "api"("org.spongepowered:configurate-yaml:4.2.0")
}
