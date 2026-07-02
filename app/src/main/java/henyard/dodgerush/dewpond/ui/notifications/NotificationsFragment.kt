package henyard.dodgerush.dewpond.ui.notifications

import android.Manifest
import android.os.Build
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import henyard.dodgerush.dewpond.R
import henyard.dodgerush.dewpond.databinding.FragmentNotificationsBinding
import henyard.dodgerush.dewpond.ui.base.ReinflatingFragment

/**
 * "Allow notifications" screen, shown after loading when the device is online.
 * Supports BOTH orientations and swaps its layout on rotation via
 * [ReinflatingFragment]. The yellow button requests the system notification
 * permission (Android 13+); either choice continues to the main menu.
 */
class NotificationsFragment : ReinflatingFragment() {

    override val layoutRes: Int = R.layout.fragment_notifications

    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { proceedToMenu() }

    override fun onBindContent(content: View) {
        val binding = FragmentNotificationsBinding.bind(content)
        binding.btnAllow.label.text = getString(R.string.notif_allow)
        binding.btnAllow.root.setOnClickListener { onAllow() }
        binding.btnDeny.label.text = getString(R.string.notif_deny)
        binding.btnDeny.root.setOnClickListener { proceedToMenu() }
    }

    private fun onAllow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            // Pre-Android 13: notifications are enabled by default, nothing to request.
            proceedToMenu()
        }
    }

    private fun proceedToMenu() {
        if (!isAdded) return
        val nav = findNavController()
        if (nav.currentDestination?.id == R.id.notificationsFragment) {
            nav.navigate(R.id.action_notifications_to_menu)
        }
    }
}
