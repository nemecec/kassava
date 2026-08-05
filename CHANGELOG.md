# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [3.0.0] - 2026-08-05

### Changed

- The artifact is published as `dev.nemecec.kassava:kassava` instead of `au.com.console:kassava`, from Maven Central instead of Bintray/JCenter.
- The Kotlin package is `dev.nemecec.kassava` instead of `au.com.console.kassava`, matching the new coordinates. Call sites are otherwise unchanged, so rewriting the import lines is the whole migration. The old package is not kept as a forwarding layer: it would clash if both artifacts ended up on one classpath.
- The project is built with Gradle 9 and Kotlin 2.4 (previously Gradle 6 and Kotlin 1.3). The published classes still target Java 8 bytecode, but consuming the library now requires a Kotlin 2.x compiler.
- `kotlin-reflect` is no longer a dependency. The library resolves the name and getter of the property references it is handed without it, so `kotlin-stdlib` is the only remaining dependency.
- The public declarations are explicitly `public` (Kotlin explicit API mode). Signatures are unchanged.

### Added

- OSGi bundle metadata and an `Automatic-Module-Name` in the jar manifest, so the jar works as-is in OSGi containers and as an automatic JPMS module.
- Sources and API documentation (Dokka) jars are published alongside the main jar.
- GitHub Actions workflows for builds, CodeQL analysis, dependency-graph submission and releases, plus weekly Dependabot updates for Gradle dependencies and actions.
- A security policy describing how to report vulnerabilities privately.
- A `NOTICE` file recording the original copyright, the copyright on the modifications, and what was changed - as the Apache License asks of a modified distribution. Redistributing this project means carrying that file along too.

### Removed

- The Travis CI configuration and the Bintray publishing setup, both defunct.
- Spek 2 and hamkrest as test dependencies; the tests are JUnit 5 with `kotlin-test` assertions. Test-only change with no effect on the published artifact.

## 2.1.0 and earlier

Releases up to and including 2.1.0 were published as `au.com.console:kassava` and
predate this changelog. See the
[release list](https://github.com/nemecec/kassava/releases) for those versions.

[Unreleased]: https://github.com/nemecec/kassava/compare/v3.0.0...HEAD
[3.0.0]: https://github.com/nemecec/kassava/compare/v2.1.0...v3.0.0
