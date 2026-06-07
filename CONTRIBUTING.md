# Contributing

Thanks for taking the time to contribute to MenuFramework.

## Getting started

1. Fork and clone the repository.
2. Make sure you have a JDK 25 toolchain available (the build auto-provisions one via the Foojay
   resolver if it is missing).
3. Build and run the full suite:

   ```bash
   ./gradlew clean test build
   ```

## Project layout

| Module | Responsibility |
|--------|----------------|
| `menu-core` | Platform-free annotations, config, compiler, state and merge logic. |
| `menu-paper` | Paper-facing facade, listener, rendering and registry. |
| `menu-folia` | Folia scheduler implementation. |
| `example-plugin` | Runnable example plugin using the public API. |

Keep platform types (Bukkit, Paper, Folia) out of `menu-core`. The domain talks in `PlayerId`,
`MenuId` and value objects; the translation to `Player` happens at the Paper edge.

## Code style

Formatting is enforced by `google-java-format` through Spotless. Run it before committing:

```bash
./gradlew spotlessApply
```

`./gradlew build` runs `spotlessCheck` and fails on unformatted code. Beyond formatting, the project
follows clean-code and Object Calisthenics conventions documented in `CLAUDE.md`: small single-purpose
methods, guard clauses over nested `if`, `Optional` over `null`, value objects over raw primitives,
and constructor injection over static singletons.

## Tests

Add or update tests for any behaviour you change. Domain logic should be testable without a running
server. New features and bug fixes are expected to ship with coverage.

## Pull requests

1. Branch from `main`.
2. Keep the change focused, with a clear description of what and why.
3. Make sure `./gradlew clean test build` is green locally; CI runs the same on Ubuntu and Windows.
4. Update `CHANGELOG.md` under a new `## [Unreleased]` section when your change is user-visible.

## Reporting bugs and requesting features

Use the issue templates. For security problems, follow `SECURITY.md` instead of opening a public
issue.
