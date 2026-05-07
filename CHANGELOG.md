# Changelog

All notable changes to MenuFramework are documented here.

## Unreleased

- No unreleased changes.

## 1.0.0 - 2026-05-07

### Added

- Added `llms.txt` as the canonical guide for AI assistants generating MenuFramework code.
- Added `jitpack.yml` with Java 21 and `publishToMavenLocal` so JitPack builds the Maven artifact directly.
- Added source jar publication for better IDE source navigation.
- Added CI coverage for `publishToMavenLocal`, catching release/JitPack publication problems before tagging.

### Changed

- Switched Maven publication to the Shadow component so the published artifact is the production fat jar.
- Updated Shadow Gradle plugin from `9.0.0-beta4` to stable `9.3.2`.
- Changed the Maven group to `com.github.HanielCota`, matching JitPack dependency coordinates.
- Removed the local Windows-only `org.gradle.java.home` from `gradle.properties`.
- Added the Foojay toolchain resolver convention for Gradle Java 21 toolchain provisioning.
- Updated usage documentation examples to match the real public API.
- Updated release workflow verification to build and publish the package before creating the GitHub release.

### Fixed

- Fixed `MenuFeatures.refreshInterval(long)` so it returns a `RefreshingMenuFeature` usable with `MenuBuilder.feature(...)`.
- Fixed Maven publication task wiring that caused `publishToMavenLocal` to fail due to implicit task dependencies.
- Fixed duplicate README installation text around shading and relocation.
