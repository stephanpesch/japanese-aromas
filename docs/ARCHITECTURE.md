# Architektur

## Module
- **`:core`** (`app.aromas.core`, Android-Library, UI-frei) — die testbare Domäne.
  - `model/` — `Aroma` (`@Serializable`, spiegelt `aromas.json`), `Language`.
  - `data/` — `AromaJsonParser` (kotlinx-serialization), `AromaRepository`
    (In-Memory-Zugriff auf die 100 Einträge).
  - `logic/` — `SeasonMatcher` (Monats-/Datumslogik wie die Web-App),
    `DistanceCalculator` (Haversine), `AromaFilter` (Saison + Kategorie + Sortierung),
    Sprach-Accessoren (`Aroma.titel(lang)` …).
  - Coverage-Gate: **≥ 80 %** (Kover); `model/` ist als reine DTO-Schicht ausgenommen.
- **`:app`** (`app.aromas`, Compose + Hilt) — UI, Standort, DI.
  - `di/DataModule` liefert das `AromaRepository` (lädt `assets/aromas.json`).
  - `ui/` — Screens + ViewModels (MVVM, `StateFlow`).

## Muster
- **MVVM + UDF**: ViewModel hält `StateFlow`; Compose rendert den Zustand.
- **Offline-first**: alle Inhalte + Bilder im APK gebündelt; nur Kartenkacheln online.
- **DI**: Hilt (`SingletonComponent`), ViewModels via `@HiltViewModel`.

## Daten
`aromas.json` + `images/NNN.jpg` in `app/src/main/assets/` sind **generierte
Snapshots** aus `../../100-aromas` (Skript `tools/sync_data.py`). Quelle der
Wahrheit bleibt `100-aromas`; nach Änderungen dort das Skript neu laufen lassen.

## Karten
MapLibre Native + OpenFreeMap-Vektor-Style (schlüsselfrei) via AndroidView im
Compose-Baum; Marker nach Primärkategorie eingefärbt. Standort über
FusedLocationProvider (nur im Vordergrund, v1).

## Geplant (als PRs)
Kartenscreen, „in der Nähe", Detailscreen, Sprachumschalter DE⇄JA,
später Näherungs-Alarme (Geofencing).
