package henyard.dodgerush.dewpond.game

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import henyard.dodgerush.dewpond.R

/** In-game effect granted by an equipped skin. */
enum class SkinAbility {
    /** No bonus (Basic Hen). */
    NONE,

    /** Slim Body: collision hitbox is 25% smaller. */
    SLIM,

    /** Tough Hat: the first hazard hit each level is ignored. */
    TOUGH,

    /** Grain Magnet: collectibles are gently pulled in from a distance. */
    MAGNET,

    /** Long Shield: the shield power-up lasts twice as long. */
    LONG_SHIELD,
}

/**
 * A single entry in the shop. Purchasable items are skins (in-game visual +
 * [ability]) with a preview and price; placeholder slots ([previewRes] == 0)
 * are shown locked with a "Level N" label as coming-soon content.
 */
data class ShopItem(
    val id: String,
    @StringRes val nameRes: Int,
    @StringRes val descRes: Int,
    val price: Int,
    @DrawableRes val previewRes: Int,
    val ability: SkinAbility = SkinAbility.NONE,
    /** Player level at which a placeholder slot is themed; also its lock label. */
    val requiredLevel: Int = 1,
) {
    /** Placeholder / coming-soon slots have no preview art. */
    val isPlaceholder: Boolean get() = previewRes == 0
}

/** Static list of shop offerings, matching the Figma Shop screen order. */
object ShopCatalog {

    /** Id of the free starter skin, owned and equipped by default. */
    const val DEFAULT_SKIN = "basic_hen"

    val items: List<ShopItem> = listOf(
        ShopItem(
            "basic_hen", R.string.shop_basic_hen, R.string.shop_basic_hen_desc,
            price = 100, previewRes = R.drawable.hen_basic_1, ability = SkinAbility.NONE,
        ),
        ShopItem(
            "slim_body", R.string.shop_slim_body, R.string.shop_slim_body_desc,
            price = 200, previewRes = R.drawable.hen_slim_1, ability = SkinAbility.SLIM,
        ),
        ShopItem(
            "tough_hat", R.string.shop_tough_hat, R.string.shop_tough_hat_desc,
            price = 300, previewRes = R.drawable.hen_tough_1, ability = SkinAbility.TOUGH,
        ),
        ShopItem(
            "grain_magnet", R.string.shop_grain_magnet, R.string.shop_grain_magnet_desc,
            price = 400, previewRes = R.drawable.hen_grain_1, ability = SkinAbility.MAGNET,
        ),
        ShopItem(
            "long_shield", R.string.shop_long_shield, R.string.shop_long_shield_desc,
            price = 500, previewRes = R.drawable.hen_long_1, ability = SkinAbility.LONG_SHIELD,
        ),
        placeholder("locked_10", 10),
        placeholder("locked_15", 15),
        placeholder("locked_20", 20),
        placeholder("locked_25", 25),
        placeholder("locked_30", 30),
        placeholder("locked_35", 35),
        placeholder("locked_40", 40),
    )

    /** Items grouped into full-screen pages for the swipeable shop grid. */
    val pages: List<List<ShopItem>> get() = items.chunked(PAGE_SIZE)

    /** The catalog entry for [id], or the default skin when unknown. */
    fun skinById(id: String): ShopItem =
        items.firstOrNull { it.id == id && !it.isPlaceholder }
            ?: items.first { it.id == DEFAULT_SKIN }

    private const val PAGE_SIZE = 9

    private fun placeholder(id: String, level: Int) =
        ShopItem(id, nameRes = 0, descRes = 0, price = 0, previewRes = 0, requiredLevel = level)
}
