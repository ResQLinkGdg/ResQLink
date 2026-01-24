package com.example.resqlink.platform.transport

interface TransportCallbacks {
    fun onEndpointFound(endpointId: String, endpointName: String? = null)
    fun onEndpointLost(endpointId: String)

    fun onConnected(endpointId: String)
    fun onDisconnected(endpointId: String)

    // ⭐ rssi nullable로 확장
    fun onPayloadReceived(
        fromEndpointId: String,
        bytes: ByteArray,
        rssi: Int? = null
    )
}
