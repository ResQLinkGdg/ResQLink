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

        // 1. 데이터 도착 로그
        Log.d("ResQLink_Net", "[수신] 데이터 도착 (From: $fromEndpointId, Size: ${bytes.size} bytes)")

        val envelope = codec.decode(bytes) ?: run {
            Log.e("ResQLink_Net", "[에러] 디코딩 실패")
            return
        }

        // 2. 메시지 정보 로그
        Log.d("ResQLink_Net", " [분석] MsgId: ${envelope.msgId}, SenderId: ${envelope.senderId}, Type: ${envelope.type}")

        if (envelope.senderId == mySenderId) {
            Log.d("ResQLink_Net", " [필터] 내가 보낸 신호이므로 처리를 중단합니다. (MyId: $mySenderId)")
            return
        }

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
