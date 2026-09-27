package app.aromas.core.model

/** UI and content language the user can switch between. */
enum class Language(
    val tag: String,
) {
    GERMAN("de"),
    JAPANESE("ja"),
    ;

    fun opposite(): Language = if (this == GERMAN) JAPANESE else GERMAN

    companion object {
        fun fromTag(tag: String?): Language = entries.firstOrNull { it.tag == tag } ?: GERMAN
    }
}
