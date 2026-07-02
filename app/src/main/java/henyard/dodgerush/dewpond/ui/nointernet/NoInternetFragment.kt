package henyard.dodgerush.dewpond.ui.nointernet

import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.navigation.fragment.findNavController
import henyard.dodgerush.dewpond.R
import henyard.dodgerush.dewpond.databinding.FragmentNoInternetBinding
import henyard.dodgerush.dewpond.ui.base.ReinflatingFragment
import henyard.dodgerush.dewpond.util.NetworkUtils

/**
 * "No internet" screen. Supports BOTH orientations (registered as a
 * free-orientation destination in MainActivity) and swaps its layout on rotation
 * via [ReinflatingFragment].
 *
 * Tapping "Restart" re-checks connectivity in place, showing a progress spinner
 * over the button instead of re-opening the loading screen. It routes to the
 * menu flow when back online, or restores the button when still offline so the
 * user can retry.
 */
class NoInternetFragment : ReinflatingFragment() {

    override val layoutRes: Int = R.layout.fragment_no_internet

    private val handler = Handler(Looper.getMainLooper())
    private var binding: FragmentNoInternetBinding? = null
    private var checking = false

    override fun onBindContent(content: View) {
        val binding = FragmentNoInternetBinding.bind(content).also { this.binding = it }
        binding.btnRestart.label.text = getString(R.string.restart)
        binding.btnRestart.root.setOnClickListener { onRestart() }
        // Restore the correct button state (e.g. after a rotation re-inflate).
        renderButton()
    }

    private fun onRestart() {
        if (checking) return
        checking = true
        renderButton()
        // Brief window so the spinner is visible before we decide.
        handler.postDelayed(::finishCheck, CHECK_DELAY_MS)
    }

    private fun finishCheck() {
        if (!isAdded || binding == null) return
        if (NetworkUtils.isOnline(requireContext())) {
            findNavController().navigate(R.id.action_no_internet_to_notifications)
        } else {
            // Still offline: restore the button so the user can retry.
            checking = false
            renderButton()
        }
    }

    private fun renderButton() {
        val binding = binding ?: return
        binding.btnRestart.root.isEnabled = !checking
        binding.btnRestart.label.visibility = if (checking) View.INVISIBLE else View.VISIBLE
        binding.restartProgress.visibility = if (checking) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
        binding = null
    }

    private companion object {
        const val CHECK_DELAY_MS = 1200L
    }
}