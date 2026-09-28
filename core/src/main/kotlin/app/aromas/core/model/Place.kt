package app.aromas.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * One curated place in Japan. A place belongs to a [PlaceCollection] (aroma
 * landscape, remarkable water, soundscape, scenic beauty). The German
 * [SerialName]s map the bundled JSON keys; the Kotlin identifiers stay English.
 *
 * [number] is the place's number within its own list (1..N, shown to the user).
 * [id] combines it with the collection so it is unique across all lists.
 * Aroma-specific fields (season, source, category) and media (image, audio) are
 * optional, since the other collections do not have them.
 */
@Serializable
data class Place(
    @SerialName("nummer") val number: Int,
    val collection: PlaceCollection = PlaceCollection.AROMA,
    @SerialName("titel_ja") val titleJa: String,
    @SerialName("titel_de") val titleDe: String,
    @SerialName("region") val region: String = "",
    @SerialName("praefektur") val prefecture: String,
    @SerialName("ort") val city: String,
    @SerialName("beschreibung_ja") val descriptionJa: String,
    @SerialName("beschreibung_de") val descriptionDe: String,
    @SerialName("lat") val lat: Double,
    @SerialName("lon") val lon: Double,
    @SerialName("bild") val image: String? = null,
    @SerialName("bild_quelle") val imageAttribution: String? = null,
    @SerialName("audio") val audio: String? = null,
    @SerialName("wikipedia") val wikipediaUrl: String? = null,
    @SerialName("untertitel_ja") val subtitleJa: String? = null,
    @SerialName("untertitel_de") val subtitleDe: String? = null,
    @SerialName("duftquelle_ja") val sourceJa: String? = null,
    @SerialName("duftquelle_de") val sourceDe: String? = null,
    @SerialName("saison_ja") val seasonJa: String? = null,
    @SerialName("saison_de") val seasonDe: String? = null,
    @SerialName("monate") val months: List<Int> = emptyList(),
    @SerialName("ganzjaehrig") val yearRound: Boolean = false,
    @SerialName("kategorie") val category: String? = null,
    @SerialName("kategorien") val categories: List<String> = emptyList(),
    @SerialName("verfuegbarkeit") val availability: List<Availability> = emptyList(),
) {
    /** Stable identity, unique across all collections (e.g. "aroma-3"). */
    val id: String get() = "${collection.name.lowercase()}-$number"
}

/**
 * When a place can actually be visited/heard/smelled: an optional day-of-year
 * range (`from`/`to` as `MM-DD`) and an optional time-of-day window
 * (`fromTime`/`toTime` as `HH:MM`). A missing bound means "unbounded on that
 * axis": no dates = any day, no times = any time. Both ranges may wrap the
 * boundary (e.g. a winter date range Dec–Feb, or a night-time window). The
 * label ([noteDe]/[noteJa], e.g. "abends" or "typische Öffnungszeiten") is shown
 * to the user; it never drives filtering.
 */
@Serializable
data class Availability(
    @SerialName("von") val from: String? = null,
    @SerialName("bis") val to: String? = null,
    @SerialName("von_zeit") val fromTime: String? = null,
    @SerialName("bis_zeit") val toTime: String? = null,
    @SerialName("hinweis_de") val noteDe: String? = null,
    @SerialName("hinweis_ja") val noteJa: String? = null,
)
