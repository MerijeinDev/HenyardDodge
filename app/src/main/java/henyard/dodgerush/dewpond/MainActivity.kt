package henyard.dodgerush.dewpond

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import henyard.dodgerush.dewpond.util.NetworkMonitor
import henyard.dodgerush.dewpond.util.SoundManager

class MainActivity : AppCompatActivity() {

    /**
     * Destinations that may be shown in BOTH orientations and rotate freely with
     * the device (Loading/Splash, NoInternet, Notifications). Everything else is
     * locked to portrait.
     */
    private val freeOrientationDestinations = setOf(
        R.id.splashFragment,
        R.id.noInternetFragment,
        R.id.notificationsFragment,
    )

    private lateinit var navController: NavController
    private val networkMonitor by lazy { NetworkMonitor(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        SoundManager.init(applicationContext)
        hideSystemBars()

        val navHost = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHost.navController
        navController.addOnDestinationChangedListener { _, destination, _ ->
            requestedOrientation = if (destination.id in freeOrientationDestinations) {
                ActivityInfo.SCREEN_ORIENTATION_SENSOR
            } else {
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        }

        // Global connectivity watch: if the network drops anywhere (menu, game,
        // etc.), jump to the NoInternet screen. Splash checks connectivity on its
        // own, and we don't re-trigger while already on NoInternet.
        networkMonitor.observe(this) { online ->
            if (!online && isActiveDestinationInterruptible()) {
                navController.navigate(R.id.action_global_no_internet)
            }
        }
    }

    private fun isActiveDestinationInterruptible(): Boolean {
        val current = navController.currentDestination?.id
        return current != null &&
            current != R.id.noInternetFragment &&
            current != R.id.splashFragment
    }

    override fun onStart() {
        super.onStart()
        SoundManager.resumeMusic(this)
    }

    override fun onStop() {
        SoundManager.pauseMusic()
        super.onStop()
    }

    override fun onDestroy() {
        SoundManager.stopMusic(releasePlayer = true)
        super.onDestroy()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemBars()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Rotation is handled without recreation (see manifest configChanges);
        // re-hide the system bars for the new orientation.
        hideSystemBars()
    }

    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
