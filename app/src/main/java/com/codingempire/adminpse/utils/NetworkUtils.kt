package com.codingempire.adminpse.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build

object NetworkUtils {

    // Function to check if the device is connected to the internet
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // For Android 10 and above (API level 29 and higher)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val network: Network? = connectivityManager.activeNetwork
            val networkCapabilities: NetworkCapabilities? =
                connectivityManager.getNetworkCapabilities(network)

            // Check if the network is capable of providing internet access
            return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        } else {
            // For Android versions below Android 10 (API level < 29), use the old deprecated method
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            return activeNetworkInfo?.isConnected == true
        }
    }
}
