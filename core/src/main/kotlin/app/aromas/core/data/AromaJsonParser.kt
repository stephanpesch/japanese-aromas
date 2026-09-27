package app.aromas.core.data

import app.aromas.core.model.Aroma
import kotlinx.serialization.json.Json
import java.io.InputStream

/** Parses the bundled aromas.json into [Aroma] objects. */
object AromaJsonParser {
    private val json = Json { ignoreUnknownKeys = true }

    fun parse(text: String): List<Aroma> = json.decodeFromString(text)

    fun parse(input: InputStream): List<Aroma> =
        parse(input.bufferedReader().use { it.readText() })
}
