package app.aromas.core

import app.aromas.core.model.Aroma

/** Builds an [Aroma] with sensible defaults for tests; override what matters. */
@Suppress("LongParameterList")
fun aroma(
    nummer: Int = 1,
    titelJa: String = "タイトル",
    titelDe: String = "Titel",
    monate: List<Int> = listOf(6, 7, 8),
    ganzjaehrig: Boolean = false,
    kategorien: List<String> = listOf("Blumen & Blüten"),
    lat: Double = 35.0,
    lon: Double = 135.0,
): Aroma =
    Aroma(
        nummer = nummer,
        titelJa = titelJa,
        titelDe = titelDe,
        region = "Kansai",
        praefektur = "Kyoto",
        ort = "Kyoto",
        duftquelleJa = "源",
        duftquelleDe = "Quelle",
        saisonJa = "6月〜8月",
        saisonDe = "Juni–August",
        monate = monate,
        ganzjaehrig = ganzjaehrig,
        kategorie = kategorien.first(),
        kategorien = kategorien,
        lat = lat,
        lon = lon,
        bild = "images/%03d.jpg".format(nummer),
        beschreibungJa = "説明",
        beschreibungDe = "Beschreibung",
    )
