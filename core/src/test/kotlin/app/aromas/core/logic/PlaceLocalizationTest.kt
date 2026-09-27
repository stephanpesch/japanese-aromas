package app.aromas.core.logic

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
}
