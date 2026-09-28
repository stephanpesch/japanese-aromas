package app.aromas.core.logic

import app.aromas.core.model.Availability
import app.aromas.core.model.Language
import app.aromas.core.model.Place

/** Language-aware accessors: pick the Japanese or German field of a [Place]. */

fun Place.title(language: Language): String = if (language == Language.JAPANESE) titleJa else titleDe

fun Place.description(language: Language): String = if (language == Language.JAPANESE) descriptionJa else descriptionDe

/** The season text (aromas only); empty for collections without a season. */
fun Place.season(language: Language): String = (if (language == Language.JAPANESE) seasonJa else seasonDe).orEmpty()

/** The scent source (aromas only); empty for collections without one. */
fun Place.source(language: Language): String = (if (language == Language.JAPANESE) sourceJa else sourceDe).orEmpty()

/**
 * A human "when" line: the season text, plus any time-of-day window or note from
 * [Place.availability] (the part the season text doesn't already convey, e.g.
 * "18:00–22:30 (abends)" or "9:00–17:00 (typische Öffnungszeiten)").
 */
fun Place.whenText(language: Language): String {
    val extras = availability.mapNotNull { it.timeNote(language) }.distinct()
    return (listOf(season(language)).filter { it.isNotEmpty() } + extras).joinToString(" · ")
}

private fun Availability.timeNote(language: Language): String? {
    val japanese = language == Language.JAPANESE
    val time = if (fromTime != null && toTime != null) "$fromTime–$toTime" else null
    val note = (if (japanese) noteJa else noteDe)?.takeIf { it.isNotEmpty() }
    return when {
        // Full-width parentheses in Japanese, matching the natural orthography.
        time != null && note != null -> if (japanese) "$time（$note）" else "$time ($note)"
        time != null -> time
        else -> note
    }
}

/** The title in the *other* language, e.g. for a bilingual subtitle. */
fun Place.secondaryTitle(language: Language): String = title(language.opposite())
