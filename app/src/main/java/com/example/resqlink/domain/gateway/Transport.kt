package com.example.resqlink.domain.gateway

interface Transport {
    fun startAdvertising()
    fun stopAdvertising()

    fun startDiscovery()
    fun stopDiscovery()

    fun connect(endpointId: String)
    fun disconnect(endpointId: String)

    fun send(to: String, bytes: ByteArray)
    fun broadcast(bytes: ByteArray)

    fun shutdown()
}
