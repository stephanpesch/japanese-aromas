package app.aromas.core.data

import app.aromas.core.aroma
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class AromaDataTest {
    private val sample =
        """
        [
          {
            "nummer": 1,
            "titel_ja": "北見のハッカとハーブ",
            "titel_de": "Minze und Kräuter von Kitami",
            "region": "Hokkaido",
            "praefektur": "Hokkaido",
            "ort": "Kitami",
            "duftquelle_ja": "ハーブ",
            "duftquelle_de": "Kräuter",
            "saison_ja": "6月〜8月",
            "saison_de": "Juni–August",
            "monate": [6, 7, 8],
            "ganzjaehrig": false,
            "kategorie": "Blumen & Blüten",
            "kategorien": ["Blumen & Blüten"],
            "lat": 43.804,
            "lon": 143.895,
            "bild": "images/001.jpg",
            "beschreibung_ja": "説明",
            "beschreibung_de": "Beschreibung",
            "extra_unbekannt": "wird ignoriert"
          }
        ]
        """.trimIndent()

    @Test
    fun `parser maps german json keys and ignores unknown fields`() {
        val list = AromaJsonParser.parse(sample)
        assertEquals(1, list.size)
        val a = list.first()
        assertEquals("北見のハッカとハーブ", a.titleJa)
        assertEquals("Minze und Kräuter von Kitami", a.titleDe)
        assertEquals(listOf(6, 7, 8), a.months)
        assertEquals(143.895, a.lon, 1e-9)
    }

    @Test
    fun `repository looks up by number and lists distinct categories`() {
        val repo =
            AromaRepository(
                listOf(
                    aroma(number = 1, categories = listOf("Meer & Küste")),
                    aroma(number = 2, categories = listOf("Meer & Küste", "Tee")),
                ),
            )
        assertEquals(2, repo.all().size)
        assertEquals(1, repo.byNumber(1)?.number)
        assertNull(repo.byNumber(99))
        assertEquals(listOf("Meer & Küste", "Tee"), repo.categories())
    }
}
