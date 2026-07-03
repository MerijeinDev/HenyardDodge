package henyard.dodgerush.dewpond.game

import androidx.annotation.DrawableRes
import henyard.dodgerush.dewpond.util.AppPrefs
import kotlin.random.Random

/** The two selectable leaderboard periods. */
enum class LbPeriod { WEEK, ALL_TIME }

/** One leaderboard row. [isPlayer] marks the current player's ("You") entry. */
data class LbEntry(
    val name: String,
    val score: Long,
    @DrawableRes val avatarRes: Int,
    val isPlayer: Boolean,
)

/**
 * Offline pseudo-leaderboard. Bots have preset scores that jitter slightly per
 * day so the board feels alive; the player's row uses their real cumulative
 * [AppPrefs.totalScore]. Rows are returned sorted by score, highest first.
 */
object Leaderboard {

    private val BOT_NAMES = listOf(
        "CluckMaster", "HenSolo", "EggSpert", "FeatherFury", "BarnBaron",
        "Cluckzilla", "SirPecks", "YolkDrop", "RoostRocket", "GrainGoblin",
        "CoopKing", "FowlPlay", "TheEggfather", "WingCommander", "BeakBandit",
        "HayHopper", "NuggetNinja", "PolloLoco", "ScrambleBoss", "CoopTrooper",
    )

    fun entries(period: LbPeriod, prefs: AppPrefs): List<LbEntry> {
        val daySeed = System.currentTimeMillis() / MILLIS_PER_DAY
        val avatars = HenCatalog.hens

        val bots = BOT_NAMES.mapIndexed { i, name ->
            val rnd = Random(daySeed * 131 + i)
            val baseAllTime = (1_950_000L - i * 82_000L) + rnd.nextLong(0, 45_000L)
            val allTime = baseAllTime.coerceAtLeast(12_000L)
            val week = (allTime * (0.15 + rnd.nextDouble() * 0.20)).toLong()
            LbEntry(
                name = name,
                score = if (period == LbPeriod.WEEK) week else allTime,
                avatarRes = avatars[i % avatars.size].thumbnailRes,
                isPlayer = false,
            )
        }

        val player = LbEntry(
            name = "You",
            score = prefs.totalScore,
            avatarRes = avatars[HenCatalog.clampIndex(prefs.selectedHenIndex)].thumbnailRes,
            isPlayer = true,
        )

        return (bots + player).sortedByDescending { it.score }
    }

    private const val MILLIS_PER_DAY = 86_400_000L
}
