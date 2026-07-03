package henyard.dodgerush.dewpond.game

import androidx.annotation.StringRes
import henyard.dodgerush.dewpond.R
import henyard.dodgerush.dewpond.util.AppPrefs

/**
 * A single achievement: a title/description plus a [goal] and a way to read the
 * player's current [progress]. Stats that aren't tracked yet simply report 0.
 */
data class Achievement(
    val id: String,
    @StringRes val titleRes: Int,
    @StringRes val descRes: Int,
    val goal: Int,
    val progress: (AppPrefs) -> Int,
) {
    /** Current progress, clamped to `0..goal`. */
    fun current(prefs: AppPrefs): Int = progress(prefs).coerceIn(0, goal)

    /** Whether the goal has been reached. */
    fun isComplete(prefs: AppPrefs): Boolean = current(prefs) >= goal

    /** Completion as a 0..100 percentage for the progress bar. */
    fun percent(prefs: AppPrefs): Int =
        if (goal <= 0) 0 else current(prefs) * 100 / goal
}

/** Static list of achievements, matching the Figma Achievements screen order. */
object AchievementCatalog {

    /** Levels the player has finished (level 1 is unlocked by default). */
    private fun clearedLevels(prefs: AppPrefs): Int =
        (prefs.unlockedLevel - 1).coerceAtLeast(0)

    val items: List<Achievement> = listOf(
        Achievement("first_dodge", R.string.ach_first_dodge, R.string.ach_first_dodge_desc, 1) {
            clearedLevels(it).coerceAtMost(1)
        },
        Achievement("untouchable", R.string.ach_untouchable, R.string.ach_untouchable_desc, 1) {
            if (it.hasFlawlessClear) 1 else 0
        },
        Achievement("grain_eater", R.string.ach_grain_eater, R.string.ach_grain_eater_desc, 5000) {
            it.lifetimeGrain
        },
        Achievement("survivor", R.string.ach_survivor, R.string.ach_survivor_desc, 1800) {
            it.surviveSeconds
        },
        Achievement("close_call", R.string.ach_close_call, R.string.ach_close_call_desc, 1000) {
            it.hazardsDodged
        },
        Achievement("yard_boss", R.string.ach_yard_boss, R.string.ach_yard_boss_desc, 20) {
            clearedLevels(it).coerceAtMost(20)
        },
        Achievement("dodge_king", R.string.ach_dodge_king, R.string.ach_dodge_king_desc, Levels.TOTAL) {
            clearedLevels(it)
        },
        Achievement("shielded", R.string.ach_shielded, R.string.ach_shielded_desc, 100) {
            it.shieldBlocks
        },
        Achievement("coin_dodger", R.string.ach_coin_dodger, R.string.ach_coin_dodger_desc, 5000) {
            it.lifetimeCoinsCollected
        },
        Achievement("fox_foiler", R.string.ach_fox_foiler, R.string.ach_fox_foiler_desc, 200) {
            it.foxDodged
        },
    )
}
