package com.example.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit

data class NetworkSnapshot(
    val isConnected: Boolean = false,
    val networkType: String = "Detecting...",
    val publicIp: String = "Resolving...",
    val latencyMs: Long = -1,
    val lastChecked: Long = 0
)

class NetworkMonitor(private val context: Context) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _networkState = MutableStateFlow(NetworkSnapshot())
    val networkState: StateFlow<NetworkSnapshot> = _networkState.asStateFlow()

    private val defaultClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    init {
        registerNetworkCallback()
    }

    private fun registerNetworkCallback() {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                updateNetworkInfo()
            }

            override fun onLost(network: Network) {
                _networkState.value = _networkState.value.copy(
                    isConnected = false,
                    networkType = "Disconnected",
                    publicIp = "Unavailable",
                    latencyMs = -1
                )
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                updateNetworkInfo()
            }
        })

        // Initial check
        updateNetworkInfo()
    }

    fun updateNetworkInfo() {
        CoroutineScope(Dispatchers.IO).launch {
            val activeNetwork = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)

            val isConnected = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
            val typeStr = when {
                capabilities == null -> "None"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi (Household Network)"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular Mobile Data"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> "Device VPN Active"
                else -> "Active Connection"
            }

            var resolvedIp = _networkState.value.publicIp
            var latency: Long = -1

            if (isConnected) {
                val ipResult = resolvePublicIp(null)
                resolvedIp = ipResult.first
                latency = ipResult.second
            } else {
                resolvedIp = "Disconnected"
            }

            _networkState.value = NetworkSnapshot(
                isConnected = isConnected,
                networkType = typeStr,
                publicIp = resolvedIp,
                latencyMs = latency,
                lastChecked = System.currentTimeMillis()
            )
        }
    }

    suspend fun resolvePublicIp(proxy: Proxy?): Pair<String, Long> = withContext(Dispatchers.IO) {
        val client = if (proxy != null) {
            OkHttpClient.Builder()
                .proxy(proxy)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build()
        } else {
            defaultClient
        }

        val request = Request.Builder()
            .url("https://api.ipify.org")
            .header("User-Agent", "SeparateInstanceHarness/1.0")
            .build()

        val start = System.currentTimeMillis()
        try {
            client.newCall(request).execute().use { response ->
                val end = System.currentTimeMillis()
                if (response.isSuccessful) {
                    val ip = response.body?.string()?.trim() ?: "Unknown"
                    Pair(ip, end - start)
                } else {
                    Pair("Lookup HTTP ${response.code}", -1)
                }
            }
        } catch (e: Exception) {
            // Fallback to secondary endpoint
            try {
                val fallbackRequest = Request.Builder()
                    .url("https://icanhazip.com")
                    .header("User-Agent", "SeparateInstanceHarness/1.0")
                    .build()
                client.newCall(fallbackRequest).execute().use { response ->
                    val end = System.currentTimeMillis()
                    if (response.isSuccessful) {
                        val ip = response.body?.string()?.trim() ?: "Unknown"
                        Pair(ip, end - start)
                    } else {
                        Pair("Failed (${e.message ?: "timeout"})", -1)
                    }
                }
            } catch (fallbackEx: Exception) {
                Pair("Unreachable (${e.javaClass.simpleName})", -1)
            }
        }
    }

    fun buildProxy(host: String, port: Int, type: String): Proxy? {
        if (host.isBlank() || port <= 0) return null
        return try {
            val proxyType = if (type.equals("SOCKS5", ignoreCase = true)) {
                Proxy.Type.SOCKS
            } else {
                Proxy.Type.HTTP
            }
            Proxy(proxyType, InetSocketAddress(host, port))
        } catch (e: Exception) {
            null
        }
    }
}
