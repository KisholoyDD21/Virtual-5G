package com.virtual5g.data.telephony

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.virtual5g.core.logging.SecureLogger
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

enum class Transport { WIFI, CELLULAR, OTHER, NONE }

/** Thin, testable wrapper around ConnectivityManager.NetworkCallback. */
class ConnectivityObserver(context: Context) {

    private val connectivityManager =
        context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    fun observe(): Flow<Transport> = callbackFlow {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        fun currentTransport(network: Network?): Transport {
            val caps = network?.let { connectivityManager.getNetworkCapabilities(it) }
                ?: return Transport.NONE
            return when {
                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> Transport.WIFI
                caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> Transport.CELLULAR
                caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> Transport.OTHER
                else -> Transport.OTHER
            }
        }

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(currentTransport(network))
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                trySend(currentTransport(network))
            }

            override fun onLost(network: Network) {
                trySend(currentTransport(connectivityManager.activeNetwork))
            }
        }

        runCatching {
            connectivityManager.registerNetworkCallback(request, callback)
        }.onFailure { error ->
            SecureLogger.e("ConnectivityObserver", "Failed to register network callback", error)
            trySend(Transport.NONE)
        }

        // Emit an initial value immediately rather than waiting for the first callback.
        trySend(currentTransport(connectivityManager.activeNetwork))

        awaitClose {
            runCatching { connectivityManager.unregisterNetworkCallback(callback) }
        }
    }.distinctUntilChanged()
}
