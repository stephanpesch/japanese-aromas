package app.aromas.core

import app.aromas.core.model.Place
import app.aromas.core.model.PlaceCollection

/** Builds a [Place] with sensible defaults for tests; override what matters. */
@Suppress("LongParameterList")
fun place(
    number: Int = 1,
    collection: PlaceCollection = PlaceCollection.AROMA,
    titleJa: String = "タイトル",
    titleDe: String = "Titel",
    months: List<Int> = listOf(6, 7, 8),
    yearRound: Boolean = false,
    categories: List<String> = listOf("Blumen & Blüten"),
    lat: Double = 35.0,
    lon: Double = 135.0,
): Place =
    Place(
        number = number,
        collection = collection,
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
        category = categories.firstOrNull(),
        categories = categories,
        lat = lat,
        lon = lon,
        image = "images/%03d.jpg".format(number),
        descriptionJa = "説明",
        descriptionDe = "Beschreibung",
    )
