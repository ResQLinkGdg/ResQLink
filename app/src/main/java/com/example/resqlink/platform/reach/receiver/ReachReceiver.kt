package com.example.resqlink.platform.reach.receiver

import android.util.Log
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
        val env = codec.decode(bytes) ?: return

        val msgId = env.msgId
        val originId = env.hops.firstOrNull()?.from ?: env.senderId

        // ✅ 내 origin(내가 최초 발신) 에코면: 처리/릴레이 모두 중단 (루프 방지 핵심)
        if (originId == mySenderId) {
            Log.d("ResQLink_Net", "[필터] 내 origin 에코. msgId=$msgId originId=$originId")
            return
        }

        // ✅ Apply는 msgId당 1회만
        val applyKey = "apply:$msgId"
        if (!dedup.isDuplicate(applyKey)) {
            dedup.mark(applyKey)
            scope.launch {
                applyIncomingSos(envelope = env, rssiDbm = rssi)
            }
        } else {
            Log.d("ResQLink_Net", "[중복-apply] msgId=$msgId")
        }

        // ✅ Relay는 msgId당 1회만
        if (env.ttl <= 0) return
        val relayKey = "relay:$msgId"
        if (dedup.isDuplicate(relayKey)) {
            Log.d("ResQLink_Net", "[중복-relay] msgId=$msgId")
            return
        }
        dedup.mark(relayKey)

        // (추가 루프 방지) hop에 내가 이미 등장하면 relay 스킵
        if (env.hops.any { it.from == mySenderId || it.to == mySenderId }) return

        val relayed = env.copy(
            senderId = mySenderId,
            ttl = env.ttl - 1,
            hops = env.hops + HopSignal(
                from = env.senderId,
                to = mySenderId,
                rssi = rssi,
                timestampMs = System.currentTimeMillis()
            )
        )

        transportProvider().broadcast(codec.encode(relayed))
    }

    override fun onEndpointFound(endpointId: String, endpointName: String?) {}
    override fun onEndpointLost(endpointId: String) {}
    override fun onConnected(endpointId: String) {}
    override fun onDisconnected(endpointId: String) {}
}
