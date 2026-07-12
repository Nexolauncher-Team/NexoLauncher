package com.nexo.launcher.utils.http

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

class NetworkUtils {
    companion object {
        /**
         * @return å½“å‰ç½‘ç»œæ˜¯å¦å·²è¿žæŽ¥
         */
        @JvmStatic
        fun isNetworkAvailable(context: Context): Boolean {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            return activeNetwork != null && (
                    capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ?: false ||
                            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ?: false ||
                            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ?: false
                    )
        }
    }
}
