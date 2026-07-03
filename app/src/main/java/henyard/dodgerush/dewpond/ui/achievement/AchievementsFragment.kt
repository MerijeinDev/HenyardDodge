package henyard.dodgerush.dewpond.ui.achievement

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import henyard.dodgerush.dewpond.R
import henyard.dodgerush.dewpond.databinding.FragmentAchievementsBinding
import henyard.dodgerush.dewpond.game.AchievementCatalog
import henyard.dodgerush.dewpond.ui.base.BaseFragment
import henyard.dodgerush.dewpond.util.AppPrefs

/** Achievements screen: a scrollable board listing every achievement with its
 * progress toward completion. */
class AchievementsFragment : BaseFragment(R.layout.fragment_achievements) {

    private lateinit var binding: FragmentAchievementsBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAchievementsBinding.bind(view)

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        binding.achList.layoutManager = LinearLayoutManager(requireContext())
        binding.achList.adapter =
            AchievementAdapter(AchievementCatalog.items, AppPrefs(requireContext()))
    }
}
