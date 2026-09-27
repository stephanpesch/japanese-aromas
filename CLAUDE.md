# CLAUDE.md — Entwickler-Workflow (japanese-aromas)

Native Android-App (Kotlin, Jetpack Compose, Hilt, MapLibre). Konventionen von
`../Takibi` übernommen.

## Build/JDK
- **JDK 21 nötig** (System-JRE 26 hat kein `javac`). Immer mit
  `export JAVA_HOME=/usr/lib/jvm/java-21-openjdk` bauen. `org.gradle.java.home`
  ist **absichtlich nicht** eingecheckt (CI nutzt Temurin 21).
- SDK-Pfad in `local.properties` (gitignored).

## Das Gate (vor jedem Merge grün)
```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
./gradlew ktlintCheck detekt test :core:koverVerify lintDebug assembleDebug
```
Grün = alle Tasks erfolgreich **und** Kover ≥ 80 % in `:core`. `ktlintFormat` fixt
Formatierung vorab. Auf GitHub erzwingt die CI dieselben Checks als Pflicht-Status
für den Merge nach `main`.

## Review vor jedem Push/Merge (Pflicht)
Vor dem Push den **`code-reviewer`-Subagenten** über den Diff laufen lassen
(Agent-Tool), Findings fixen, dann erst committen/pushen. Der Reviewer prüft u. a.:
englische Bezeichner/Kommentare, `:core`-Schichtung, Tests/Coverage, detekt-Fallen,
Null-/Coroutine-Sicherheit, MapLibre-/Standort-Lifecycle.

## Regeln
- **Sprache im Code:** Bezeichner **und** Kommentare Englisch. Nutzertexte nur in
  `values/` (Deutsch) + `values-ja/` (Japanisch); japanische Inhalte kommen aus
  `aromas.json`.
- **Schichten:** testbare Domain-Logik nach `:core` (UI-frei); UI/Standort/DI nach
  `:app`. `SeasonMatcher` muss der Web-App (`100-aromas`) entsprechen.
- **Daten:** `aromas.json` + `images/` sind generiert (`tools/sync_data.py`) — nie
  von Hand editieren; Quelle ist `../../100-aromas`.

## Arbeiten über Pull Requests
`main` ist geschützt. Jedes Feature: Branch → lokales code-reviewer-Review → Push →
PR → CI grün → Merge.

## Geräteprüfung
Nach `:app:installDebug` die App auf dem Gerät testen und
`adb logcat -d -b crash` prüfen (keine Abstürze), bevor etwas als „fertig" gilt.
