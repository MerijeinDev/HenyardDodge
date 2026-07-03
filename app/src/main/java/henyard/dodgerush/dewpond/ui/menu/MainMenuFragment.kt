package henyard.dodgerush.dewpond.ui.menu

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import henyard.dodgerush.dewpond.R
import henyard.dodgerush.dewpond.databinding.FragmentMainMenuBinding
import henyard.dodgerush.dewpond.ui.base.BaseFragment
import henyard.dodgerush.dewpond.util.AppPrefs

/**
 * Main menu (portrait). Entry hub for Play, Shop, Achievements, Profile, Leaderboard.
 */
class MainMenuFragment : BaseFragment(R.layout.fragment_main_menu) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentMainMenuBinding.bind(view)

        binding.coinsText.text = "%,d".format(AppPrefs(requireContext()).coins)

        binding.btnPlay.setOnClickListener {
            findNavController().navigate(R.id.action_menu_to_level_select)
        }
        binding.btnSettings.setOnClickListener { notImplemented("Settings") }
        binding.btnShop.setOnClickListener {
            findNavController().navigate(R.id.action_menu_to_shop)
        }
        binding.btnAchievements.setOnClickListener {
            findNavController().navigate(R.id.action_menu_to_achievements)
        }
        binding.btnProfile.setOnClickListener { notImplemented("Profile") }
        binding.btnLeaderboard.setOnClickListener { notImplemented("Leaderboard") }
    }

    private fun notImplemented(name: String) {
        Toast.makeText(requireContext(), "$name — soon", Toast.LENGTH_SHORT).show()
    }
}
