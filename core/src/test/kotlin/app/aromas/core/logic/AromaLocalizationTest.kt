package app.aromas.core.logic

import app.aromas.core.aroma
import app.aromas.core.model.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AromaLocalizationTest {
    private val a = aroma(titleJa = "北見", titleDe = "Kitami")

    @Test
    fun `japanese picks the ja fields`() {
        assertEquals("北見", a.title(Language.JAPANESE))
        assertEquals(a.descriptionJa, a.description(Language.JAPANESE))
        assertEquals(a.seasonJa, a.season(Language.JAPANESE))
        assertEquals(a.sourceJa, a.source(Language.JAPANESE))
    }

    @Test
    fun `german picks the de fields`() {
        assertEquals("Kitami", a.title(Language.GERMAN))
        assertEquals(a.descriptionDe, a.description(Language.GERMAN))
        assertEquals(a.seasonDe, a.season(Language.GERMAN))
        assertEquals(a.sourceDe, a.source(Language.GERMAN))
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
        assertEquals("Kitami", a.secondaryTitle(Language.JAPANESE))
        assertEquals("北見", a.secondaryTitle(Language.GERMAN))
    }
}
