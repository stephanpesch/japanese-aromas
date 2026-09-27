package app.aromas.core.logic

import app.aromas.core.model.Aroma
import app.aromas.core.model.Language

/** Language-aware accessors: pick the Japanese or German field of an [Aroma]. */

fun Aroma.titel(language: Language): String =
    if (language == Language.JAPANESE) titelJa else titelDe

fun Aroma.beschreibung(language: Language): String =
    if (language == Language.JAPANESE) beschreibungJa else beschreibungDe

fun Aroma.saison(language: Language): String =
    if (language == Language.JAPANESE) saisonJa else saisonDe

fun Aroma.duftquelle(language: Language): String =
    if (language == Language.JAPANESE) duftquelleJa else duftquelleDe
