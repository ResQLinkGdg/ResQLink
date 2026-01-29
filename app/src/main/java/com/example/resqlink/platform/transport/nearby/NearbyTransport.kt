package com.example.resqlink.platform.transport.nearby

import android.content.Context
import android.util.Log
import com.example.resqlink.domain.gateway.Transport
import com.example.resqlink.platform.transport.TransportCallbacks
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import java.util.concurrent.ConcurrentHashMap

class NearbyTransport(
    context: Context,
    private val callbacks: TransportCallbacks,
    private val serviceId: String = NearbyConfig.SERVICE_ID,
    private val localEndpointName: String = NearbyConfig.deviceName(),
    private val strategy: Strategy = Strategy.P2P_CLUSTER
) : Transport {

    private val client: ConnectionsClient =
        Nearby.getConnectionsClient(context.applicationContext)

    private val connectedEndpoints = ConcurrentHashMap.newKeySet<String>()

    private var advertising = false
    private var discovering = false

    // -----------------------------
    // Payload 수신
    // -----------------------------
    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            payload.asBytes()?.let {
                callbacks.onPayloadReceived(
                    fromEndpointId = endpointId,
                    bytes = it,
                    rssi = null // ⭐ Nearby에서는 못 구함
                )
            }
        }

        override fun onPayloadTransferUpdate(
            endpointId: String,
            update: PayloadTransferUpdate
        ) {
            // MVP에서는 무시
        }
    }

    // -----------------------------
    // 연결 생명주기
    // -----------------------------
    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {

        override fun onConnectionInitiated(
            endpointId: String,
            info: ConnectionInfo
        ) {
            Log.d(TAG, "Connection initiated: $endpointId (${info.endpointName})")
            client.acceptConnection(endpointId, payloadCallback)
        }

        override fun onConnectionResult(
            endpointId: String,
            result: ConnectionResolution
        ) {
            if (result.status.statusCode == ConnectionsStatusCodes.STATUS_OK) {
                connectedEndpoints.add(endpointId)
                callbacks.onConnected(endpointId)
                Log.d(TAG, "Connected: $endpointId")
            } else {
                Log.w(TAG, "Connection failed: $endpointId")
            }
        }

        override fun onDisconnected(endpointId: String) {
            connectedEndpoints.remove(endpointId)
            callbacks.onDisconnected(endpointId)
            Log.d(TAG, "Disconnected: $endpointId")
        }
    }

    // -----------------------------
    // 탐색 콜백
    // -----------------------------
    private val discoveryCallback = object : EndpointDiscoveryCallback() {

        override fun onEndpointFound(
            endpointId: String,
            info: DiscoveredEndpointInfo
        ) {
            callbacks.onEndpointFound(endpointId, info.endpointName)
            Log.d(TAG, "Endpoint found: $endpointId")

            // ⭐ 충돌 방지: 내 이름보다 상대방 이름이 사전순으로 뒤에 있을 때만 내가 먼저 연결 시도
            if (localEndpointName < (info.endpointName ?: "")) {
                Log.d(TAG, "I am the initiator for $endpointId")
                connect(endpointId)
            } else {
                Log.d(TAG, "Waiting for $endpointId to connect to me")
            }
        }

        override fun onEndpointLost(endpointId: String) {
            callbacks.onEndpointLost(endpointId)
            Log.d(TAG, "Endpoint lost: $endpointId")
        }
    }

    // -----------------------------
    // Transport API 구현
    // -----------------------------
    override fun startAdvertising() {
        if (advertising) return

        val options = AdvertisingOptions.Builder()
            .setStrategy(strategy)
            .build()

        client.startAdvertising(
            localEndpointName,
            serviceId,
            connectionLifecycleCallback,
            options
        ).addOnSuccessListener {
            advertising = true
            Log.d(TAG, "Advertising started")
        }.addOnFailureListener {
            Log.e(TAG, "Advertising failed", it)
        }
    }

    override fun stopAdvertising() {
        if (!advertising) return
        client.stopAdvertising()
        advertising = false
    }

    override fun startDiscovery() {
        if (discovering) return

        val options = DiscoveryOptions.Builder()
            .setStrategy(strategy)
            .build()

        client.startDiscovery(
            serviceId,
            discoveryCallback,
            options
        ).addOnSuccessListener {
            discovering = true
            Log.d(TAG, "Discovery started")
        }.addOnFailureListener {
            Log.e(TAG, "Discovery failed", it)
        }
    }

    override fun stopDiscovery() {
        if (!discovering) return
        client.stopDiscovery()
        discovering = false
    }

    override fun connect(endpointId: String) {
        if (connectedEndpoints.contains(endpointId)) return

        client.requestConnection(
            localEndpointName,
            endpointId,
            connectionLifecycleCallback
        ).addOnFailureListener {
            Log.e(TAG, "Request connection failed: $endpointId", it)
        }
    }

    override fun disconnect(endpointId: String) {
        client.disconnectFromEndpoint(endpointId)
        connectedEndpoints.remove(endpointId)
    }

    override fun send(to: String, bytes: ByteArray) {
        if (!connectedEndpoints.contains(to)) return

        client.sendPayload(to, Payload.fromBytes(bytes))
            .addOnFailureListener {
                Log.e(TAG, "Send failed: $to", it)
            }
    }

    override fun broadcast(bytes: ByteArray) {
        if (connectedEndpoints.isEmpty()) return

        client.sendPayload(
            connectedEndpoints.toList(),
            Payload.fromBytes(bytes)
        ).addOnFailureListener {
            Log.e(TAG, "Broadcast failed", it)
        }
    }

    override fun shutdown() {
        stopDiscovery()
        stopAdvertising()
        client.stopAllEndpoints()
        connectedEndpoints.clear()
    }

    companion object {
        private const val TAG = "NearbyTransport"
    }
}
