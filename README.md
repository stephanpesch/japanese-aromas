# Japanese Aromas 🌸

Native Android-App zu den **100 Duft-Landschaften Japans** (かおり風景100選) — zum
standortbewussten „Durch-Japan-Gehen": Karte, „in der Nähe"-Liste, Detailseiten,
Filter nach Reisezeit und Kategorie, umschaltbar Deutsch ⇄ 日本語.

Die Daten (100 zweisprachige Einträge + Fotos + geprüfte Koordinaten) stammen aus
dem Schwesterprojekt `../../100-aromas` und werden als Snapshot gebündelt.

## Bauen & Installieren

Braucht ein **JDK 21** (das System-JRE 26 hat kein `javac`). SDK-Pfad in
`local.properties` (`sdk.dir=…`, nicht eingecheckt).

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
./gradlew assembleDebug          # APK bauen
./gradlew :app:installDebug      # auf verbundenes Gerät installieren
```

## Qualität (lokal wie in CI)

Ein Befehl prüft alles — **muss grün sein, bevor gemerged wird**:

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
./gradlew ktlintCheck detekt test :core:koverVerify lintDebug assembleDebug
```

- **ktlint** + **detekt** — Formatierung & statische Analyse (`./gradlew ktlintFormat` fixt Formatierung).
- **Kover** — ≥ 80 % Zeilenabdeckung in `:core`.
- Auf GitHub erzwingt die **CI** (`.github/workflows/ci.yml`) genau diese Checks als
  Pflicht vor jedem Merge (Branch Protection auf `main`). Das **Code-Review** läuft
  lokal über den `code-reviewer`-Subagenten vor jedem Push (siehe `CLAUDE.md`).

## Struktur

- **`:core`** — `app.aromas.core`, UI-frei & testbar: Datenmodell, JSON-Parser,
  Repository, Logik (`SeasonMatcher`, `DistanceCalculator`, `AromaFilter`,
  Sprach-Accessoren). Hier gilt das 80 %-Coverage-Gate.
- **`:app`** — `app.aromas`, Jetpack Compose + Hilt: Screens, Standort, DI,
  Sprachumschalter.
- **Daten:** `app/src/main/assets/aromas.json` + `images/` (generiert). Nach einer
  Datenänderung in `100-aromas`: `python3 tools/sync_data.py` neu laufen lassen.

## Karten

**MapLibre** mit freiem **OpenFreeMap**-Style — **kein API-Key, kein Konto**. Nur die
Kartenkacheln brauchen Internet; Liste/Filter/Fotos/Distanz laufen offline.

## Mitarbeiten

Arbeit läuft über **Pull Requests** gegen `main` (geschützt). Details zum
Entwickler-Workflow in `CLAUDE.md`, Architektur in `docs/ARCHITECTURE.md`.
