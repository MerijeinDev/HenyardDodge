package henyard.dodgerush.dewpond.game

/** Static level catalog and per-level tuning for the game. */
object Levels {

    /** Total number of playable levels shown on the level-select grid. */
    const val TOTAL = 40

    /**
     * Per-level difficulty knobs. Hazards are introduced gradually and the
     * flow gets denser/faster while bonuses grow rarer at higher levels.
     */
    data class Tuning(
        val durationMs: Long,
        val hazards: Set<EntityKind>,
        /** Obstacle spawn density (higher = more obstacles). */
        val densityMul: Float,
        /** Falling/dashing speed multiplier. */
        val speedMul: Float,
        /** Power-up spacing multiplier (higher = rarer bonuses). */
        val bonusRarityMul: Float,
        /** Coins awarded for completing the level. */
        val coinReward: Int,
    )

    /** Coerces [level] into the valid 1..[TOTAL] range. */
    fun clamp(level: Int): Int = level.coerceIn(1, TOTAL)

    /** Round length grows with the level; first five follow the design spec. */
    fun durationMs(level: Int): Long = tuningFor(level).durationMs

    /** Coins granted for finishing [level], by 10-level bands (100/200/300/400). */
    fun coinReward(level: Int): Int = (((clamp(level) - 1) / 10) + 1) * 100

    /** Resolves the difficulty tuning for [level]. */
    fun tuningFor(level: Int): Tuning {
        val lvl = clamp(level)
        val duration = when (lvl) {
            1 -> 30_000L
            2 -> 40_000L
            3 -> 50_000L
            4 -> 60_000L
            5 -> 75_000L
            else -> 75_000L + (lvl - 5) * 2_000L
        }
        val hazards: Set<EntityKind> = when (lvl) {
            1 -> setOf(EntityKind.HAY)
            2 -> setOf(EntityKind.HAY, EntityKind.BARREL)
            3 -> setOf(EntityKind.HAY, EntityKind.BARREL, EntityKind.TOOL_1, EntityKind.TOOL_2)
            4 -> setOf(
                EntityKind.HAY, EntityKind.BARREL,
                EntityKind.TOOL_1, EntityKind.TOOL_2, EntityKind.FOX,
            )
            else -> setOf(
                EntityKind.HAY, EntityKind.BARREL,
                EntityKind.TOOL_1, EntityKind.TOOL_2, EntityKind.FOX,
            )
        }
        val beyond = (lvl - 5).coerceAtLeast(0)
        val density = when (lvl) {
            1 -> 0.9f
            2 -> 1.2f
            3 -> 1.35f
            4 -> 1.6f
            5 -> 1.9f
            else -> (1.9f + beyond * 0.05f).coerceAtMost(2.8f)
        }
        val speed = when (lvl) {
            1 -> 0.9f
            2 -> 1.0f
            3 -> 1.1f
            4 -> 1.25f
            5 -> 1.4f
            else -> (1.4f + beyond * 0.03f).coerceAtMost(2.0f)
        }
        val bonusRarity = when (lvl) {
            1 -> 0.7f
            2 -> 0.9f
            3 -> 1.1f
            4 -> 1.3f
            5 -> 1.6f
            else -> (1.6f + beyond * 0.05f).coerceAtMost(2.2f)
        }
        return Tuning(duration, hazards, density, speed, bonusRarity, coinReward(lvl))
    }
}
