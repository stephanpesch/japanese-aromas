package app.aromas.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * One of the 100 "aroma landscapes". The German [SerialName]s map the bundled
 * aromas.json keys (generated from the 100-aromas dataset); the Kotlin
 * identifiers stay English per the project convention.
 */
@Serializable
data class Aroma(
    @SerialName("nummer") val number: Int,
    @SerialName("titel_ja") val titleJa: String,
    @SerialName("titel_de") val titleDe: String,
    @SerialName("region") val region: String,
    @SerialName("praefektur") val prefecture: String,
    @SerialName("ort") val city: String,
    @SerialName("duftquelle_ja") val sourceJa: String,
    @SerialName("duftquelle_de") val sourceDe: String,
    @SerialName("saison_ja") val seasonJa: String,
    @SerialName("saison_de") val seasonDe: String,
    @SerialName("monate") val months: List<Int>,
    @SerialName("ganzjaehrig") val yearRound: Boolean,
    @SerialName("kategorie") val category: String,
    @SerialName("kategorien") val categories: List<String>,
    @SerialName("lat") val lat: Double,
    @SerialName("lon") val lon: Double,
    @SerialName("bild") val image: String,
    @SerialName("beschreibung_ja") val descriptionJa: String,
    @SerialName("beschreibung_de") val descriptionDe: String,
)
