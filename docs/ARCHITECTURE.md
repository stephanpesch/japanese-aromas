# Architecture

## Modules
- **`:core`** (`app.aromas.core`, Android library, UI-free) — the testable domain.
  - `model/` — `Aroma` (`@Serializable`, mirrors `aromas.json`), `Language`.
  - `data/` — `AromaJsonParser` (kotlinx-serialization), `AromaRepository`
    (in-memory access to the 100 entries).
  - `logic/` — `SeasonMatcher` (month/date logic like the web app),
    `DistanceCalculator` (Haversine), `AromaFilter` (season + category + sorting),
    language accessors (`Aroma.title(language)` …).
  - Coverage gate: **≥ 80 %** (Kover); `model/` is excluded as a pure DTO layer.
- **`:app`** (`app.aromas`, Compose + Hilt) — UI, location, DI.
  - `di/DataModule` provides the `AromaRepository` (loads `assets/aromas.json`).
  - `ui/` — screens + view models (MVVM, `StateFlow`), `ui/map/` the MapLibre screen.

## Patterns
- **MVVM + UDF:** the view model holds a `StateFlow`; Compose renders the state.
- **Offline-first:** all content + images are bundled in the APK; only the map tiles
  are fetched online.
- **DI:** Hilt (`SingletonComponent`); view models via `@HiltViewModel`.

## Data
`aromas.json` + `images/NNN.jpg` in `app/src/main/assets/` are **generated
snapshots** from `../../100-aromas` (via `tools/sync_data.py`). The source of truth
stays in `100-aromas`; re-run the script after changing it.

## Maps
MapLibre Native (OpenGL renderer) + the keyless OpenFreeMap vector style, hosted in
an `AndroidView` inside the Compose tree; markers coloured by primary category.
Location will use FusedLocationProvider (foreground only, v1).

## Language
English is used everywhere in the codebase and docs. The only non-English content
is the user-facing UI strings (`values/` German, `values-ja/` Japanese) and the
bundled aroma dataset.

## Planned (as PRs)
Nearby (location + distance), detail screen, map filters (season/category),
later proximity alerts (geofencing).
