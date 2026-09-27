package app.aromas.core.logic

import app.aromas.core.aroma
import app.aromas.core.model.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AromaLocalizationTest {
    private val a = aroma(titelJa = "北見", titelDe = "Kitami")

    @Test
    fun `japanese picks the ja fields`() {
        assertEquals("北見", a.titel(Language.JAPANESE))
        assertEquals(a.beschreibungJa, a.beschreibung(Language.JAPANESE))
        assertEquals(a.saisonJa, a.saison(Language.JAPANESE))
        assertEquals(a.duftquelleJa, a.duftquelle(Language.JAPANESE))
    }

    @Test
    fun `german picks the de fields`() {
        assertEquals("Kitami", a.titel(Language.GERMAN))
        assertEquals(a.beschreibungDe, a.beschreibung(Language.GERMAN))
        assertEquals(a.saisonDe, a.saison(Language.GERMAN))
        assertEquals(a.duftquelleDe, a.duftquelle(Language.GERMAN))
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
        assertEquals("Kitami", a.secondaryTitel(Language.JAPANESE))
        assertEquals("北見", a.secondaryTitel(Language.GERMAN))
    }
}
