package henyard.dodgerush.dewpond.game

/** Static level catalog and per-level tuning for the game. */
object Levels {

    /** Total number of playable levels shown on the level-select grid. */
    const val TOTAL = 40

    /** Coerces [level] into the valid 1..[TOTAL] range. */
    fun clamp(level: Int): Int = level.coerceIn(1, TOTAL)

    /** Round length grows a little with each level to ramp up difficulty. */
    fun durationMs(level: Int): Long = 40_000L + (clamp(level) - 1) * 3_000L
}
