package henyard.dodgerush.dewpond.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.LiveData

/**
 * Observes the device network state and emits `true` while at least one network
 * with validated internet access is available, `false` otherwise.
 *
 * Implemented as a [LiveData] so it registers its callback only while observed
 * (activity STARTED) and cleans up automatically.
 */
class NetworkMonitor(context: Context) : LiveData<Boolean>() {

    private val appContext = context.applicationContext
    private val cm = appContext
        .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val validatedNetworks = mutableSetOf<Network>()

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
            val online = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            if (online) validatedNetworks.add(network) else validatedNetworks.remove(network)
            postValue(validatedNetworks.isNotEmpty())
        }

        override fun onLost(network: Network) {
            validatedNetworks.remove(network)
            postValue(validatedNetworks.isNotEmpty())
        }
    }

    override fun onActive() {
        validatedNetworks.clear()
        postValue(NetworkUtils.isOnline(appContext))
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        runCatching { cm.registerNetworkCallback(request, callback) }
    }

    override fun onInactive() {
        runCatching { cm.unregisterNetworkCallback(callback) }
    }
}
