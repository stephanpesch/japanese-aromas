package app.aromas.core.logic

import app.aromas.core.model.Availability
import app.aromas.core.model.Language
import app.aromas.core.model.PlaceCollection
import app.aromas.core.place
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PlaceLocalizationTest {
    private val p = place(titleJa = "北見", titleDe = "Kitami")

    @Test
    fun `japanese picks the ja fields`() {
        assertEquals("北見", p.title(Language.JAPANESE))
        assertEquals(p.descriptionJa, p.description(Language.JAPANESE))
        assertEquals(p.seasonJa, p.season(Language.JAPANESE))
        assertEquals(p.sourceJa, p.source(Language.JAPANESE))
    }

    @Test
    fun `german picks the de fields`() {
        assertEquals("Kitami", p.title(Language.GERMAN))
        assertEquals(p.descriptionDe, p.description(Language.GERMAN))
        assertEquals(p.seasonDe, p.season(Language.GERMAN))
        assertEquals(p.sourceDe, p.source(Language.GERMAN))
    }

    @Test
    fun `optional season and source are empty when absent`() {
        val water = place(number = 5, collection = PlaceCollection.WATER).copy(seasonJa = null, sourceDe = null)
        assertEquals("", water.season(Language.JAPANESE))
        assertEquals("", water.source(Language.GERMAN))
    }

    @Test
    fun `language tag round-trips`() {
        assertEquals(Language.JAPANESE, Language.fromTag("ja"))
        assertEquals(Language.GERMAN, Language.fromTag("de"))
        assertEquals(Language.GERMAN, Language.fromTag(null))
    }

    @Test
    fun `opposite flips the language`() {
        assertEquals(Language.JAPANESE, Language.GERMAN.opposite())
        assertEquals(Language.GERMAN, Language.JAPANESE.opposite())
    }

    @Test
    fun `secondary title is the other language`() {
        assertEquals("Kitami", p.secondaryTitle(Language.JAPANESE))
        assertEquals("北見", p.secondaryTitle(Language.GERMAN))
    }

    @Test
    fun `whenText is the plain season when there is no availability`() {
        assertEquals(p.seasonDe, p.whenText(Language.GERMAN))
    }

    @Test
    fun `whenText appends the time window and note`() {
        val festival =
            place(
                availability =
                    listOf(
                        Availability(
                            from = "08-12",
                            to = "08-15",
                            fromTime = "18:00",
                            toTime = "22:30",
                            noteDe = "abends",
                        ),
                    ),
            ).copy(seasonDe = "12.–15. August")
        assertEquals("12.–15. August · 18:00–22:30 (abends)", festival.whenText(Language.GERMAN))
    }

    @Test
    fun `whenText uses full-width parentheses for the japanese note`() {
        val festival =
            place(availability = listOf(Availability(fromTime = "18:00", toTime = "22:30", noteJa = "夜")))
                .copy(seasonJa = "8月12〜15日")
        assertEquals("8月12〜15日 · 18:00–22:30（夜）", festival.whenText(Language.JAPANESE))
    }

    @Test
    fun `whenText shows a note alone when there is no time window`() {
        val hours =
            place(
                availability = listOf(Availability(noteDe = "typische Öffnungszeiten")),
            ).copy(seasonDe = "ganzjährig")
        assertEquals("ganzjährig · typische Öffnungszeiten", hours.whenText(Language.GERMAN))
    }
}
