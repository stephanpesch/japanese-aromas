package app.aromas.core.logic

import app.aromas.core.model.Aroma
import app.aromas.core.model.Language

/** Language-aware accessors: pick the Japanese or German field of an [Aroma]. */

fun Aroma.title(language: Language): String = if (language == Language.JAPANESE) titleJa else titleDe

fun Aroma.description(language: Language): String = if (language == Language.JAPANESE) descriptionJa else descriptionDe

fun Aroma.season(language: Language): String = if (language == Language.JAPANESE) seasonJa else seasonDe

fun Aroma.source(language: Language): String = if (language == Language.JAPANESE) sourceJa else sourceDe

/** The title in the *other* language, e.g. for a bilingual subtitle. */
fun Aroma.secondaryTitle(language: Language): String = title(language.opposite())
