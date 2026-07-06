package henyard.dodgerush.dewpond.ui.shop

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import henyard.dodgerush.dewpond.R
import henyard.dodgerush.dewpond.databinding.ItemShopBinding
import henyard.dodgerush.dewpond.game.ShopItem
import henyard.dodgerush.dewpond.util.AppPrefs
import henyard.dodgerush.dewpond.util.setClickSound

/**
 * Renders a single page's [ShopItem]s as cards. [onBuy] performs the purchase
 * and returns true on success, in which case the card refreshes to "owned".
 */
class ShopItemAdapter(
    private val items: List<ShopItem>,
    private val prefs: AppPrefs,
    private val onBuy: (ShopItem) -> Boolean,
    private val onEquip: (ShopItem) -> Unit,
) : RecyclerView.Adapter<ShopItemAdapter.ShopHolder>() {

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopHolder {
        val binding = ItemShopBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ShopHolder(binding, prefs, onBuy, onEquip)
    }

    override fun onBindViewHolder(holder: ShopHolder, position: Int) {
        holder.bind(items[position])
    }

    class ShopHolder(
        private val binding: ItemShopBinding,
        private val prefs: AppPrefs,
        private val onBuy: (ShopItem) -> Boolean,
        private val onEquip: (ShopItem) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ShopItem) {
            if (item.isPlaceholder) bindLocked(item) else bindPurchasable(item)
        }

        private fun bindLocked(item: ShopItem) {
            val context = binding.root.context
            binding.cardBg.setImageResource(R.drawable.shop_plate_lock)
            binding.itemTitle.setOutlineColor(context.getColor(R.color.shop_lock_stroke))
            binding.itemTitle.text = context.getString(R.string.shop_locked_level, item.requiredLevel)
            binding.itemTitle.visibility = View.VISIBLE
            binding.itemPreview.visibility = View.INVISIBLE
            binding.itemDesc.visibility = View.INVISIBLE
            binding.priceButton.visibility = View.GONE
            binding.priceButton.setOnClickListener(null)
        }

        private fun bindPurchasable(item: ShopItem) {
            val context = binding.root.context
            binding.cardBg.setImageResource(R.drawable.shop_plate)
            binding.itemTitle.setOutlineColor(context.getColor(R.color.shop_text_brown))
            binding.itemTitle.visibility = View.VISIBLE
            binding.itemTitle.setText(item.nameRes)
            binding.itemPreview.visibility = View.VISIBLE
            binding.itemPreview.setImageResource(item.previewRes)
            binding.itemDesc.visibility = View.VISIBLE
            binding.itemDesc.setText(item.descRes)
            binding.priceButton.visibility = View.VISIBLE

            when {
                prefs.equippedSkin == item.id -> {
                    binding.priceCoin.visibility = View.GONE
                    binding.priceText.setText(R.string.shop_equipped)
                    binding.priceButton.isClickable = false
                    binding.priceButton.setOnClickListener(null)
                }
                prefs.ownsShopItem(item.id) -> {
                    binding.priceCoin.visibility = View.GONE
                    binding.priceText.setText(R.string.shop_equip)
                    binding.priceButton.isClickable = true
                    binding.priceButton.setClickSound {
                        onEquip(item)
                        bind(item)
                    }
                }
                else -> {
                    binding.priceCoin.visibility = View.VISIBLE
                    binding.priceText.text = item.price.toString()
                    binding.priceButton.isClickable = true
                    binding.priceButton.setClickSound { if (onBuy(item)) bind(item) }
                }
            }
        }
    }
}
