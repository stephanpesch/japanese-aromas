package app.aromas.core.logic

/**
 * The four seasons as a travel-time filter. Each season covers three consecutive
 * calendar months (1-12) starting at spring in March; selecting seasons yields
 * the union of their months, which feeds [SeasonMatcher] / [AromaFilter].
 */
enum class Season {
    SPRING,
    SUMMER,
    AUTUMN,
    WINTER,
    ;

    /** The calendar months (1-12) this season covers. */
    val months: Set<Int>
        get() {
            val firstMonth = FIRST_MONTH + ordinal * MONTHS_PER_SEASON
            return (0 until MONTHS_PER_SEASON)
                .map { (firstMonth - 1 + it) % MONTHS_PER_YEAR + 1 }
                .toSet()
        }

    private companion object {
        const val FIRST_MONTH = 3 // March = start of spring
        const val MONTHS_PER_SEASON = 3
        const val MONTHS_PER_YEAR = 12
    }
}
