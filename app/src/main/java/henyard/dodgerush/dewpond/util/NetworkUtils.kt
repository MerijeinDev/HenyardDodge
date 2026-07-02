package henyard.dodgerush.dewpond.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/** Small helper to check the current network state synchronously. */
object NetworkUtils {

    /**
     * Returns true if there is an active network that reports internet
     * capability and has been validated (i.e. actually reaches the internet).
     */
    fun isOnline(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE)
            as? ConnectivityManager ?: return false
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}
