package henyard.dodgerush.dewpond.game

import androidx.annotation.DrawableRes
import henyard.dodgerush.dewpond.R

/**
 * The selectable hens shown on the ChooseYourHen screen.
 *
 * [thumbnailRes] is the card artwork used in the picker grid (exported from
 * Figma); [spriteRes] is the sprite drawn as the player in [GameView].
 */
data class Hen(
    @DrawableRes val thumbnailRes: Int,
    @DrawableRes val spriteRes: Int,
)

object HenCatalog {

    val hens: List<Hen> = listOf(
        Hen(R.drawable.hen_card_1, R.drawable.hen_run),
        Hen(R.drawable.hen_card_2, R.drawable.hen_basic_1),
        Hen(R.drawable.hen_card_3, R.drawable.hen_slim_1),
        Hen(R.drawable.hen_card_4, R.drawable.hen_tough_1),
        Hen(R.drawable.hen_card_5, R.drawable.hen_long_1),
        Hen(R.drawable.hen_card_6, R.drawable.hen_grain_1),
    )

    fun clampIndex(index: Int): Int = index.coerceIn(0, hens.lastIndex)

    @DrawableRes
    fun spriteFor(index: Int): Int = hens[clampIndex(index)].spriteRes
}
