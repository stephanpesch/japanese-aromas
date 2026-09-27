package app.aromas.core.data

import app.aromas.core.model.Place
import kotlinx.serialization.json.Json
import java.io.InputStream

/** Parses a bundled places JSON file into [Place] objects. */
object PlaceJsonParser {
    private val json = Json { ignoreUnknownKeys = true }

    fun parse(text: String): List<Place> = json.decodeFromString(text)

    fun parse(input: InputStream): List<Place> = parse(input.bufferedReader().use { it.readText() })
}
