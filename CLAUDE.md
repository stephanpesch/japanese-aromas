# CLAUDE.md — developer workflow (japanese-aromas)

Native Android app (Kotlin, Jetpack Compose, Hilt, MapLibre). Conventions mirrored
from `../Takibi`.

## Build / JDK
- **JDK 21 required** (the system JRE 26 has no `javac`). Always build with
  `export JAVA_HOME=/usr/lib/jvm/java-21-openjdk`. `org.gradle.java.home` is
  **deliberately not** committed (CI uses Temurin 21).
- SDK path in `local.properties` (gitignored).

## The gate (green before every merge)
```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
./gradlew ktlintCheck detekt test :core:koverVerify lintDebug assembleDebug
```
Green = all tasks succeed **and** Kover ≥ 80 % in `:core`. `ktlintFormat` fixes
formatting first. On GitHub, CI enforces the same checks as a required status for
merging into `main`.

## Review before every push/merge (required)
Before pushing, run the **`code-reviewer` subagent** over the diff (Agent tool),
fix the findings, then commit/push. It checks: English everywhere, `:core` layering,
tests/coverage, detekt traps, null-/coroutine-safety, MapLibre/location lifecycle.

## Rules
- **English everywhere:** identifiers, comments, KDoc, commit messages, PR text and
  docs are English. The **only** non-English content is the app's user-facing
  strings (`values/` German, `values-ja/` Japanese) and the bundled aroma data
  (`aromas.json`; its German JSON keys are mapped via `@SerialName`, never used as
  identifiers).
- **Layers:** testable domain logic goes into `:core` (UI-free); UI/location/DI into
  `:app`. `SeasonMatcher` must match the web app (`100-aromas`).
- **Data:** `aromas.json` + `images/` are generated (`tools/sync_data.py`) — never
  hand-edit; the source of truth is `../../100-aromas`.

## Working through pull requests
`main` is protected. Every feature: branch → local code-reviewer → push → PR → CI
green → merge.

## Device / emulator verification
After `:app:installDebug`, test the app on a device/emulator and check
`adb logcat -d -b crash` (no crashes) before calling anything "done". A headless
emulator can be created as an AVD under `/tmp` (limited disk on the main volume);
capture UI with `adb exec-out screencap -p`. Note: MapLibre must use the
**OpenGL** renderer (`org.maplibre.gl:android-sdk-opengl`) — the default Vulkan
renderer aborts on the emulator's gfxstream driver.
