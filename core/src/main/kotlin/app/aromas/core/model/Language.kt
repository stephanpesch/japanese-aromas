package app.aromas.core.model

/** UI and content language the user can switch between. */
enum class Language(val tag: String) {
    GERMAN("de"),
    JAPANESE("ja"),
    ;

    companion object {
        fun fromTag(tag: String?): Language =
            entries.firstOrNull { it.tag == tag } ?: GERMAN
    }
}
