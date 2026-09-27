package app.aromas.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * One of the 100 "aroma landscapes". Fields mirror the bundled aromas.json
 * (generated from the 100-aromas dataset by tools/sync_data.py).
 */
@Serializable
data class Aroma(
    val nummer: Int,
    @SerialName("titel_ja") val titelJa: String,
    @SerialName("titel_de") val titelDe: String,
    val region: String,
    val praefektur: String,
    val ort: String,
    @SerialName("duftquelle_ja") val duftquelleJa: String,
    @SerialName("duftquelle_de") val duftquelleDe: String,
    @SerialName("saison_ja") val saisonJa: String,
    @SerialName("saison_de") val saisonDe: String,
    val monate: List<Int>,
    val ganzjaehrig: Boolean,
    val kategorie: String,
    val kategorien: List<String>,
    val lat: Double,
    val lon: Double,
    val bild: String,
    @SerialName("beschreibung_ja") val beschreibungJa: String,
    @SerialName("beschreibung_de") val beschreibungDe: String,
)
