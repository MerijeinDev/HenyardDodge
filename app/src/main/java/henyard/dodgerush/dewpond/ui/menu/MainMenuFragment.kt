package henyard.dodgerush.dewpond.ui.menu

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import henyard.dodgerush.dewpond.R
import henyard.dodgerush.dewpond.databinding.FragmentMainMenuBinding
import henyard.dodgerush.dewpond.ui.base.BaseFragment

/**
 * Main menu (portrait). Entry hub for Play, Shop, Achievements, Profile, Leaderboard.
 */
class MainMenuFragment : BaseFragment(R.layout.fragment_main_menu) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentMainMenuBinding.bind(view)

        binding.btnPlay.setOnClickListener {
            findNavController().navigate(R.id.action_menu_to_game)
        }
        binding.btnSettings.setOnClickListener { notImplemented("Settings") }
        binding.btnShop.setOnClickListener { notImplemented("Shop") }
        binding.btnAchievements.setOnClickListener { notImplemented("Achievements") }
        binding.btnProfile.setOnClickListener { notImplemented("Profile") }
        binding.btnLeaderboard.setOnClickListener { notImplemented("Leaderboard") }
    }

    private fun notImplemented(name: String) {
        Toast.makeText(requireContext(), "$name — soon", Toast.LENGTH_SHORT).show()
    }
}
