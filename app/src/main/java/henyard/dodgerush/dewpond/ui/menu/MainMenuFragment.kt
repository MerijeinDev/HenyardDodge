package henyard.dodgerush.dewpond.ui.menu

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import henyard.dodgerush.dewpond.R
import henyard.dodgerush.dewpond.databinding.FragmentMainMenuBinding
import henyard.dodgerush.dewpond.ui.base.BaseFragment
import henyard.dodgerush.dewpond.util.AppPrefs
import henyard.dodgerush.dewpond.util.SoundManager
import henyard.dodgerush.dewpond.util.setClickSound

/**
 * Main menu (portrait). Entry hub for Play, Shop, Achievements, Profile, Leaderboard.
 */
class MainMenuFragment : BaseFragment(R.layout.fragment_main_menu) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentMainMenuBinding.bind(view)

        binding.coinsText.text = "%,d".format(AppPrefs(requireContext()).coins)

        SoundManager.init(requireContext())
        SoundManager.startMusic(requireContext())

        binding.btnPlay.setClickSound {
            findNavController().navigate(R.id.action_menu_to_level_select)
        }
        binding.btnSettings.setClickSound {
            findNavController().navigate(R.id.action_menu_to_settings)
        }
        binding.btnShop.setClickSound {
            findNavController().navigate(R.id.action_menu_to_shop)
        }
        binding.btnAchievements.setClickSound {
            findNavController().navigate(R.id.action_menu_to_achievements)
        }
        binding.btnProfile.setClickSound {
            findNavController().navigate(R.id.action_menu_to_profile)
        }
        binding.btnLeaderboard.setClickSound {
            findNavController().navigate(R.id.action_menu_to_leaderboard)
        }
    }
}
