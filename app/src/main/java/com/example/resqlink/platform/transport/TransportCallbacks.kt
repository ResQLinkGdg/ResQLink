package com.example.resqlink.platform.transport

interface TransportCallbacks {
    fun onEndpointFound(endpointId: String, endpointName: String? = null)
    fun onEndpointLost(endpointId: String)

    fun onConnected(endpointId: String)
    fun onDisconnected(endpointId: String)

    fun onPayloadReceived(fromEndpointId: String, bytes: ByteArray)
}
