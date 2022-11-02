package com.arianmanesh.atmospher.main

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class SharedViewModel(application: Application) : AndroidViewModel(application) {

    private val context : Application = application
    private var internet = false

    private val _internetConnection = MutableLiveData<Boolean>()
    val internetConnection: LiveData<Boolean>
        get() = _internetConnection

    fun checkInternetConnectivity(){
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
        val capabilities = manager.getNetworkCapabilities(manager.activeNetwork)
        if (capabilities != null) {
            if (
                !capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) &&
                !capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
            ) {
                _internetConnection.postValue(false)
                internet = false
            }else{
                internet = true
            }
        }

        manager.registerNetworkCallback(networkRequest, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _internetConnection.postValue(true)
                internet = true
            }
            override fun onLost(network: Network) {
                _internetConnection.postValue(false)
                internet = false
            }
            override fun onUnavailable() {
                _internetConnection.postValue(false)
                internet = false
            }
        })
    }

    fun checkInternetState() : Boolean{
        return internet
    }

}