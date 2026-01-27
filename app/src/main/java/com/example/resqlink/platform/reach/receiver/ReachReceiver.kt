package com.example.resqlink.platform.reach.receiver

import com.example.resqlink.domain.gateway.Transport
import com.example.resqlink.platform.reach.dedup.DedupStore
import com.example.resqlink.platform.reach.protocol.HopSignal
import com.example.resqlink.platform.reach.protocol.MessageCodec
import com.example.resqlink.platform.transport.TransportCallbacks

class ReachReceiver(
    private val transport: Transport,
    private val codec: MessageCodec,
    private val dedup: DedupStore,
    private val mySenderId: String
) : TransportCallbacks {

    override fun onPayloadReceived(fromEndpointId: String, bytes: ByteArray, rssi: Int?) {
        val envelope = codec.decode(bytes)

        if (dedup.isDuplicate(envelope.msgId)) return
        if (envelope.ttl <= 0) return

        val hop = HopSignal(
            from = envelope.senderId,
            to = mySenderId,
            rssi = rssi,
            timestampMs = System.currentTimeMillis()
        )

        val relayed = envelope.copy(
            senderId = mySenderId,
            ttl = envelope.ttl - 1,
            hops = envelope.hops + hop
        )

        transport.broadcast(codec.encode(relayed))
    }

    override fun onEndpointFound(endpointId: String, endpointName: String?) {}
    override fun onEndpointLost(endpointId: String) {}
    override fun onConnected(endpointId: String) {}
    override fun onDisconnected(endpointId: String) {}
}
