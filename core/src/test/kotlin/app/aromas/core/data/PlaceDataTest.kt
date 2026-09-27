package app.aromas.core.data

import app.aromas.core.model.PlaceCollection
import app.aromas.core.place
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class PlaceDataTest {
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
    fun `parser maps german json keys, defaults the collection and ignores unknown fields`() {
        val list = PlaceJsonParser.parse(sample)
        assertEquals(1, list.size)
        val p = list.first()
        assertEquals("北見のハッカとハーブ", p.titleJa)
        assertEquals(PlaceCollection.AROMA, p.collection)
        assertEquals("aroma-1", p.id)
        assertEquals(listOf(6, 7, 8), p.months)
        assertEquals(143.895, p.lon, 1e-9)
    }

    @Test
    fun `repository looks up by id and lists distinct categories`() {
        val repo =
            PlaceRepository(
                listOf(
                    place(number = 1, categories = listOf("Meer & Küste")),
                    place(number = 2, categories = listOf("Meer & Küste", "Tee")),
                ),
            )
        assertEquals(2, repo.all().size)
        assertEquals(1, repo.byId("aroma-1")?.number)
        assertNull(repo.byId("aroma-99"))
        assertEquals(listOf("Meer & Küste", "Tee"), repo.categories())
    }

    @Test
    fun `byCollection returns only places of that collection`() {
        val repo =
            PlaceRepository(
                listOf(
                    place(number = 1, collection = PlaceCollection.AROMA),
                    place(number = 2, collection = PlaceCollection.WATER),
                    place(number = 3, collection = PlaceCollection.WATER),
                ),
            )
        assertEquals(listOf(2, 3), repo.byCollection(PlaceCollection.WATER).map { it.number })
        assertEquals(1, repo.byCollection(PlaceCollection.AROMA).size)
    }
}
