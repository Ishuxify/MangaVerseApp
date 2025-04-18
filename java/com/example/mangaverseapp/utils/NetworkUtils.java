package com.example.mangaverseapp.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

public class NetworkUtils {

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        // Check if the device is connected to a network
        if (connectivityManager != null) {
            Network network = connectivityManager.getActiveNetwork();
            if (network != null) {
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
                // Check if the network is connected and has internet capability
                if (capabilities != null) {
                    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
                }
            }
        }
        return false;
    }
}
