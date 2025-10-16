package com.toh.wearossample.helper.networkstate

interface NetworkStateListener {
    fun onNetworkAvailable()
    fun onNetworkLost() {}
}
