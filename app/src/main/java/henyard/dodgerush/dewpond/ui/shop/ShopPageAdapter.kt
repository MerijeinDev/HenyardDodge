package henyard.dodgerush.dewpond.ui.shop

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import henyard.dodgerush.dewpond.databinding.PageShopGridBinding
import henyard.dodgerush.dewpond.game.ShopItem
import henyard.dodgerush.dewpond.util.AppPrefs

/** One swipeable page = a fixed 3-column grid of the page's [ShopItem]s. */
class ShopPageAdapter(
    private val pages: List<List<ShopItem>>,
    private val prefs: AppPrefs,
    private val onBuy: (ShopItem) -> Boolean,
) : RecyclerView.Adapter<ShopPageAdapter.PageHolder>() {

    override fun getItemCount() = pages.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageHolder {
        val binding = PageShopGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PageHolder(binding)
    }

    override fun onBindViewHolder(holder: PageHolder, position: Int) {
        holder.bind(pages[position], prefs, onBuy)
    }

    class PageHolder(
        private val binding: PageShopGridBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(items: List<ShopItem>, prefs: AppPrefs, onBuy: (ShopItem) -> Boolean) {
            binding.pageGrid.layoutManager = GridLayoutManager(binding.root.context, COLUMNS)
            binding.pageGrid.adapter = ShopItemAdapter(items, prefs, onBuy)
        }
    }

    companion object {
        private const val COLUMNS = 3
    }
}
