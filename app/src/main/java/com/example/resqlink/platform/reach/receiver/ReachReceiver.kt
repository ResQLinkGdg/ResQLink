package com.example.resqlink.platform.reach.receiver

import com.example.resqlink.domain.gateway.Transport
import com.example.resqlink.domain.usecase.radar.ApplyIncomingSosUsecase
import com.example.resqlink.platform.reach.dedup.DedupStore
import com.example.resqlink.platform.reach.protocol.HopSignal
import com.example.resqlink.platform.reach.protocol.MessageCodec
import com.example.resqlink.platform.transport.TransportCallbacks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ReachReceiver(
    private val transportProvider: () -> Transport,
    private val codec: MessageCodec,
    private val dedup: DedupStore,
    private val mySenderId: String,
    private val applyIncomingSos: ApplyIncomingSosUsecase,
    private val scope: CoroutineScope
) : TransportCallbacks {

    override fun onPayloadReceived(fromEndpointId: String, bytes: ByteArray, rssi: Int?) {
        val envelope = codec.decode(bytes)?: return

        if (dedup.isDuplicate(envelope.msgId)) return

        scope.launch {
            applyIncomingSos(envelope = envelope, rssiDbm = rssi)
        }

        if (envelope.ttl <= 0) return

        val currentTransport = transportProvider()

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

        currentTransport.broadcast(codec.encode(relayed))
    }

    override fun onEndpointFound(endpointId: String, endpointName: String?) {}
    override fun onEndpointLost(endpointId: String) {}
    override fun onConnected(endpointId: String) {}
    override fun onDisconnected(endpointId: String) {}
}
