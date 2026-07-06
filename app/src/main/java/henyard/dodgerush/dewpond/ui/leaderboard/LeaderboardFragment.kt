package henyard.dodgerush.dewpond.ui.leaderboard

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import henyard.dodgerush.dewpond.R
import henyard.dodgerush.dewpond.databinding.FragmentLeaderboardBinding
import henyard.dodgerush.dewpond.game.LbPeriod
import henyard.dodgerush.dewpond.game.Leaderboard
import henyard.dodgerush.dewpond.ui.base.BaseFragment
import henyard.dodgerush.dewpond.util.AppPrefs
import henyard.dodgerush.dewpond.util.setBackSound
import henyard.dodgerush.dewpond.util.setClickSound

/** Leaderboard screen: an offline pseudo-ranking of bots plus the player,
 * switchable between This Week and All Time. */
class LeaderboardFragment : BaseFragment(R.layout.fragment_leaderboard) {

    private lateinit var prefs: AppPrefs
    private lateinit var binding: FragmentLeaderboardBinding
    private var period = LbPeriod.WEEK

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLeaderboardBinding.bind(view)
        prefs = AppPrefs(requireContext())

        binding.btnBack.setBackSound { findNavController().popBackStack() }
        binding.lbList.layoutManager = LinearLayoutManager(requireContext())

        binding.tabWeek.setClickSound { selectPeriod(LbPeriod.WEEK) }
        binding.tabAllTime.setClickSound { selectPeriod(LbPeriod.ALL_TIME) }

        selectPeriod(period)
    }

    private fun selectPeriod(next: LbPeriod) {
        period = next
        styleTab(binding.tabWeek, next == LbPeriod.WEEK)
        styleTab(binding.tabAllTime, next == LbPeriod.ALL_TIME)

        val entries = Leaderboard.entries(next, prefs)
        binding.lbList.adapter = LeaderboardAdapter(entries)

        val playerIndex = entries.indexOfFirst { it.isPlayer }
        if (playerIndex >= 0) binding.lbList.scrollToPosition(playerIndex)
    }

    private fun styleTab(tab: TextView, active: Boolean) {
        tab.setBackgroundResource(if (active) R.drawable.lb_tab_active else 0)
        tab.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (active) R.color.ach_text else R.color.lb_tab_inactive_text
            )
        )
    }
}
