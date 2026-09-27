package app.aromas.core

import app.aromas.core.model.Aroma

/** Builds an [Aroma] with sensible defaults for tests; override what matters. */
@Suppress("LongParameterList")
fun aroma(
    number: Int = 1,
    titleJa: String = "タイトル",
    titleDe: String = "Titel",
    months: List<Int> = listOf(6, 7, 8),
    yearRound: Boolean = false,
    categories: List<String> = listOf("Blumen & Blüten"),
    lat: Double = 35.0,
    lon: Double = 135.0,
): Aroma =
    Aroma(
        number = number,
        titleJa = titleJa,
        titleDe = titleDe,
        region = "Kansai",
        prefecture = "Kyoto",
        city = "Kyoto",
        sourceJa = "源",
        sourceDe = "Quelle",
        seasonJa = "6月〜8月",
        seasonDe = "Juni–August",
        months = months,
        yearRound = yearRound,
        category = categories.first(),
        categories = categories,
        lat = lat,
        lon = lon,
        image = "images/%03d.jpg".format(number),
        descriptionJa = "説明",
        descriptionDe = "Beschreibung",
    )
