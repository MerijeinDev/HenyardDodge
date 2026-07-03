package henyard.dodgerush.dewpond.util

import android.content.Context

/** Lightweight persisted app flags backed by SharedPreferences. */
class AppPrefs(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(NAME, Context.MODE_PRIVATE)

    /** Whether the how-to-play onboarding has already been shown. */
    var onboardingSeen: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_SEEN, false)
        set(value) = prefs.edit().putBoolean(KEY_ONBOARDING_SEEN, value).apply()

    /** Index into [henyard.dodgerush.dewpond.game.HenCatalog.hens] of the chosen hen. */
    var selectedHenIndex: Int
        get() = prefs.getInt(KEY_HEN_INDEX, 0)
        set(value) = prefs.edit().putInt(KEY_HEN_INDEX, value).apply()

    /** Player nickname (empty until the player sets one). */
    var nickname: String
        get() = prefs.getString(KEY_NICKNAME, "").orEmpty()
        set(value) = prefs.edit().putString(KEY_NICKNAME, value).apply()

    /** Highest level the player has unlocked (level 1 is always available). */
    var unlockedLevel: Int
        get() = prefs.getInt(KEY_UNLOCKED_LEVEL, 1)
        set(value) = prefs.edit().putInt(KEY_UNLOCKED_LEVEL, value).apply()

    /** Player's coin balance, earned from finishing games and spent in the shop. */
    var coins: Int
        get() = prefs.getInt(KEY_COINS, 0)
        set(value) = prefs.edit().putInt(KEY_COINS, value.coerceAtLeast(0)).apply()

    /** Cumulative score across every run the player has finished. */
    var totalScore: Long
        get() = prefs.getLong(KEY_TOTAL_SCORE, 0L)
        set(value) = prefs.edit().putLong(KEY_TOTAL_SCORE, value.coerceAtLeast(0L)).apply()

    /** Highest score achieved in a single run. */
    var bestRunScore: Int
        get() = prefs.getInt(KEY_BEST_RUN_SCORE, 0)
        set(value) = prefs.edit().putInt(KEY_BEST_RUN_SCORE, value.coerceAtLeast(0)).apply()

    /** Longest streak (in seconds) survived without getting hit. */
    var longestNoHitSeconds: Int
        get() = prefs.getInt(KEY_LONGEST_NO_HIT, 0)
        set(value) = prefs.edit().putInt(KEY_LONGEST_NO_HIT, value.coerceAtLeast(0)).apply()

    /** Records a finished run's [score], updating the cumulative and best totals. */
    fun recordRunScore(score: Int) {
        val safe = score.coerceAtLeast(0)
        prefs.edit()
            .putLong(KEY_TOTAL_SCORE, totalScore + safe)
            .putInt(KEY_BEST_RUN_SCORE, maxOf(bestRunScore, safe))
            .apply()
    }

    // ---- Lifetime counters used to drive achievements ----
    val lifetimeGrain: Int get() = prefs.getInt(KEY_GRAIN, 0)
    val lifetimeCoinsCollected: Int get() = prefs.getInt(KEY_COINS_COLLECTED, 0)
    val hazardsDodged: Int get() = prefs.getInt(KEY_HAZARDS_DODGED, 0)
    val foxDodged: Int get() = prefs.getInt(KEY_FOX_DODGED, 0)
    val shieldBlocks: Int get() = prefs.getInt(KEY_SHIELD_BLOCKS, 0)
    val surviveSeconds: Int get() = prefs.getInt(KEY_SURVIVE_SECONDS, 0)
    val hasUntouchableWin: Boolean get() = prefs.getBoolean(KEY_UNTOUCHABLE, false)

    /**
     * Accumulates a run's gameplay stats into the lifetime counters that back
     * the achievements and profile records.
     */
    fun recordRunStats(
        grain: Int,
        coinsCollected: Int,
        hazardsDodged: Int,
        foxDodged: Int,
        shieldBlocks: Int,
        survivedSeconds: Int,
        longestNoHitSeconds: Int,
        untouchableWin: Boolean,
    ) {
        prefs.edit()
            .putInt(KEY_GRAIN, lifetimeGrain + grain.coerceAtLeast(0))
            .putInt(KEY_COINS_COLLECTED, lifetimeCoinsCollected + coinsCollected.coerceAtLeast(0))
            .putInt(KEY_HAZARDS_DODGED, this.hazardsDodged + hazardsDodged.coerceAtLeast(0))
            .putInt(KEY_FOX_DODGED, this.foxDodged + foxDodged.coerceAtLeast(0))
            .putInt(KEY_SHIELD_BLOCKS, this.shieldBlocks + shieldBlocks.coerceAtLeast(0))
            .putInt(KEY_SURVIVE_SECONDS, surviveSeconds + survivedSeconds.coerceAtLeast(0))
            .putInt(KEY_LONGEST_NO_HIT, maxOf(this.longestNoHitSeconds, longestNoHitSeconds.coerceAtLeast(0)))
            .putBoolean(KEY_UNTOUCHABLE, hasUntouchableWin || untouchableWin)
            .apply()
    }

    // ---- Settings ----
    var musicEnabled: Boolean
        get() = prefs.getBoolean(KEY_MUSIC, true)
        set(value) = prefs.edit().putBoolean(KEY_MUSIC, value).apply()

    var sfxEnabled: Boolean
        get() = prefs.getBoolean(KEY_SFX, true)
        set(value) = prefs.edit().putBoolean(KEY_SFX, value).apply()

    /** Whether a shop item (by id) has been purchased. The default skin is free. */
    fun ownsShopItem(id: String): Boolean =
        id == henyard.dodgerush.dewpond.game.ShopCatalog.DEFAULT_SKIN ||
            prefs.getStringSet(KEY_OWNED_ITEMS, emptySet()).orEmpty().contains(id)

    /** Marks a shop item as owned. */
    fun setShopItemOwned(id: String) {
        val owned = prefs.getStringSet(KEY_OWNED_ITEMS, emptySet()).orEmpty().toMutableSet()
        if (owned.add(id)) prefs.edit().putStringSet(KEY_OWNED_ITEMS, owned).apply()
    }

    /** Currently equipped skin id (drives the in-game chicken visual + ability). */
    var equippedSkin: String
        get() = prefs.getString(KEY_EQUIPPED_SKIN, henyard.dodgerush.dewpond.game.ShopCatalog.DEFAULT_SKIN)
            .orEmpty().ifBlank { henyard.dodgerush.dewpond.game.ShopCatalog.DEFAULT_SKIN }
        set(value) = prefs.edit().putString(KEY_EQUIPPED_SKIN, value).apply()

    /** Best star rating (0..3) earned on a given level; 0 means not yet cleared. */
    fun starsForLevel(level: Int): Int = prefs.getInt(KEY_STARS_PREFIX + level, 0)

    /**
     * Records the outcome of finishing [level] with [stars] (keeping the best
     * rating) and unlocks the next level when the current frontier is cleared.
     */
    fun recordLevelWin(level: Int, stars: Int) {
        val best = maxOf(starsForLevel(level), stars.coerceIn(0, 3))
        val editor = prefs.edit().putInt(KEY_STARS_PREFIX + level, best)
        if (level >= unlockedLevel) editor.putInt(KEY_UNLOCKED_LEVEL, level + 1)
        editor.apply()
    }

    private companion object {
        const val NAME = "henyard_prefs"
        const val KEY_ONBOARDING_SEEN = "onboarding_seen"
        const val KEY_HEN_INDEX = "hen_index"
        const val KEY_NICKNAME = "nickname"
        const val KEY_UNLOCKED_LEVEL = "unlocked_level"
        const val KEY_STARS_PREFIX = "stars_level_"
        const val KEY_COINS = "coins"
        const val KEY_OWNED_ITEMS = "owned_shop_items"
        const val KEY_TOTAL_SCORE = "total_score"
        const val KEY_BEST_RUN_SCORE = "best_run_score"
        const val KEY_LONGEST_NO_HIT = "longest_no_hit"
        const val KEY_EQUIPPED_SKIN = "equipped_skin"
        const val KEY_GRAIN = "lifetime_grain"
        const val KEY_COINS_COLLECTED = "lifetime_coins_collected"
        const val KEY_HAZARDS_DODGED = "hazards_dodged"
        const val KEY_FOX_DODGED = "fox_dodged"
        const val KEY_SHIELD_BLOCKS = "shield_blocks"
        const val KEY_SURVIVE_SECONDS = "survive_seconds"
        const val KEY_UNTOUCHABLE = "untouchable_win"
        const val KEY_MUSIC = "music_enabled"
        const val KEY_SFX = "sfx_enabled"
    }
}
