package app.aromas.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** The curated "100 of Japan" lists a [Place] can belong to. */
@Serializable
enum class PlaceCollection {
    @SerialName("aroma")
    AROMA,

    @SerialName("water")
    WATER,

    @SerialName("sound")
    SOUND,

    @SerialName("scenery")
    SCENERY,
}
