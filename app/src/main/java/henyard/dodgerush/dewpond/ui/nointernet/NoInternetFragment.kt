package henyard.dodgerush.dewpond.ui.nointernet

import android.view.View
import androidx.navigation.fragment.findNavController
import henyard.dodgerush.dewpond.R
import henyard.dodgerush.dewpond.databinding.FragmentNoInternetBinding
import henyard.dodgerush.dewpond.ui.base.ReinflatingFragment

/**
 * "No internet" screen. Supports BOTH orientations (registered as a
 * free-orientation destination in MainActivity) and swaps its layout on rotation
 * via [ReinflatingFragment].
 */
class NoInternetFragment : ReinflatingFragment() {

    override val layoutRes: Int = R.layout.fragment_no_internet

    override fun onBindContent(content: View) {
        val binding = FragmentNoInternetBinding.bind(content)
        binding.btnRestart.label.text = getString(R.string.restart)
        binding.btnRestart.root.setOnClickListener { onRestart() }
    }

    private fun onRestart() {
        // Re-run the loading screen, which re-checks connectivity and routes
        // to the menu (online) or back here (still offline).
        findNavController().navigate(R.id.action_no_internet_to_splash)
    }
}
