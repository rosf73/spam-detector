package com.spamdetector.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class NetworkManager { companion object {

    @RequiresApi(Build.VERSION_CODES.M)
    fun checkNetwork(context: Context) :Boolean     //네트워크가 끊겨있는지 검사
    {
        var result = false
        val connectivityManager = context.getSystemService(AppCompatActivity.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if(capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) or capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)))
                result = true
        return result
    }
}}