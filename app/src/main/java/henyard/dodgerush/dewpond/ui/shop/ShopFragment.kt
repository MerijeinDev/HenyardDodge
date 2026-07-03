package henyard.dodgerush.dewpond.ui.shop

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.view.children
import androidx.core.view.setMargins
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import henyard.dodgerush.dewpond.R
import henyard.dodgerush.dewpond.databinding.FragmentShopBinding
import henyard.dodgerush.dewpond.game.ShopCatalog
import henyard.dodgerush.dewpond.game.ShopItem
import henyard.dodgerush.dewpond.ui.base.BaseFragment
import henyard.dodgerush.dewpond.util.AppPrefs

/**
 * Shop screen: horizontally swipeable pages, each a 3-column grid of power-up
 * cards. Unlocked cards can be bought with coins; locked placeholders (Level N)
 * fill the later pages as coming-soon content.
 */
class ShopFragment : BaseFragment(R.layout.fragment_shop) {

    private lateinit var prefs: AppPrefs
    private lateinit var binding: FragmentShopBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentShopBinding.bind(view)
        prefs = AppPrefs(requireContext())

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
        updateBalance()

        val pages = ShopCatalog.pages
        binding.shopPager.adapter = ShopPageAdapter(pages, prefs, ::onBuy)

        buildDots(pages.size)
        binding.shopPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) = selectDot(position)
            }
        )
    }

    /** Attempts to buy [item]; returns true when the purchase succeeds. */
    private fun onBuy(item: ShopItem): Boolean {
        if (prefs.ownsShopItem(item.id)) return false
        if (prefs.coins < item.price) {
            Toast.makeText(requireContext(), R.string.shop_not_enough, Toast.LENGTH_SHORT).show()
            return false
        }
        prefs.coins -= item.price
        prefs.setShopItemOwned(item.id)
        updateBalance()
        return true
    }

    private fun updateBalance() {
        binding.coinsText.text = "%,d".format(prefs.coins)
    }

    private fun buildDots(count: Int) {
        binding.pageDots.removeAllViews()
        val size = (resources.displayMetrics.density * DOT_SIZE_DP).toInt()
        val margin = (resources.displayMetrics.density * DOT_MARGIN_DP).toInt()
        repeat(count) {
            val dot = ImageView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(size, size).apply { setMargins(margin) }
                setImageResource(R.drawable.shop_dot_inactive)
            }
            binding.pageDots.addView(dot)
        }
        selectDot(0)
    }

    private fun selectDot(selected: Int) {
        binding.pageDots.children.forEachIndexed { i, dot ->
            (dot as ImageView).setImageResource(
                if (i == selected) R.drawable.shop_dot_active else R.drawable.shop_dot_inactive
            )
        }
    }

    private companion object {
        const val DOT_SIZE_DP = 10
        const val DOT_MARGIN_DP = 5
    }
}
