# Japanese Aromas 🌸

Native Android app for the **100 Fragrance Landscapes of Japan** (かおり風景100選) —
a location-aware companion for walking through Japan: a map, a "nearby" list, detail
pages, filters by travel time and category, switchable between German and 日本語.

The data (100 bilingual entries + photos + verified coordinates) comes from the
sibling project `../../100-aromas` and is bundled as a snapshot.

> Note: the app's user interface is German/Japanese by design; everything else in
> this repository (code, comments, docs, commits) is English.

## Build & install

Requires a **JDK 21** (the system JRE 26 has no `javac`). The SDK path goes into
`local.properties` (`sdk.dir=…`, not committed).

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
./gradlew assembleDebug          # build the APK
./gradlew :app:installDebug      # install on a connected device/emulator
```

## Quality (local, same as CI)

One command checks everything — it **must be green before merging**:

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
./gradlew ktlintCheck detekt test :core:koverVerify lintDebug assembleDebug
```

- **ktlint** + **detekt** — formatting & static analysis (`./gradlew ktlintFormat` fixes formatting).
- **Kover** — ≥ 80 % line coverage in `:core`.
- On GitHub, **CI** (`.github/workflows/ci.yml`) enforces these checks as a required
  status before every merge (branch protection on `main`). **Code review** runs
  locally via the `code-reviewer` subagent before every push (see `CLAUDE.md`).

## Structure

- **`:core`** — `app.aromas.core`, UI-free & testable: data model, JSON parser,
  repository, logic (`SeasonMatcher`, `DistanceCalculator`, `AromaFilter`, language
  accessors). The 80 % coverage gate applies here.
- **`:app`** — `app.aromas`, Jetpack Compose + Hilt: screens, location, DI, language
  switch.
- **Data:** `app/src/main/assets/aromas.json` + `images/` (generated). After changing
  the data in `100-aromas`, re-run `python3 tools/sync_data.py`.

## Maps

**MapLibre** with the free **OpenFreeMap** style — **no API key, no account**. Only
the map tiles need the internet; list/filter/photos/distance work offline. Uses the
OpenGL renderer (`org.maplibre.gl:android-sdk-opengl`).

## Contributing

Work happens through **pull requests** against `main` (protected). See `CLAUDE.md`
for the developer workflow and `docs/ARCHITECTURE.md` for the architecture.
