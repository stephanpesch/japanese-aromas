package app.aromas.core.logic

import app.aromas.core.model.Language
import app.aromas.core.model.Place

/** Language-aware accessors: pick the Japanese or German field of a [Place]. */

fun Place.title(language: Language): String = if (language == Language.JAPANESE) titleJa else titleDe

fun Place.description(language: Language): String = if (language == Language.JAPANESE) descriptionJa else descriptionDe

/** The season text (aromas only); empty for collections without a season. */
fun Place.season(language: Language): String = (if (language == Language.JAPANESE) seasonJa else seasonDe).orEmpty()

/** The scent source (aromas only); empty for collections without one. */
fun Place.source(language: Language): String = (if (language == Language.JAPANESE) sourceJa else sourceDe).orEmpty()

/** The title in the *other* language, e.g. for a bilingual subtitle. */
fun Place.secondaryTitle(language: Language): String = title(language.opposite())
