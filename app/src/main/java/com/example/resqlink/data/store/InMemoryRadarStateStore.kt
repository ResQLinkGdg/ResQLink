package com.example.resqlink.data.store

import com.example.resqlink.domain.gateway.GeoLocation
import com.example.resqlink.domain.model.Range.RangeBucket
import com.example.resqlink.domain.model.Range.RssiRangeConfig
import com.example.resqlink.domain.model.radar.RadarMode
import com.example.resqlink.domain.model.radar.RadarTarget
import com.example.resqlink.domain.policy.ranging.RssiRangeEstimator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

class InMemoryRadarStateStore(
    private val rssiCfg: RssiRangeConfig = RssiRangeConfig(),
    private val keepMs: Long = 24 * 60 * 60 * 1000L // 최근 24h 메모리 유지(앱 재실행하면 초기화)
): RadarStateStore {
    private val estimator = RssiRangeEstimator(rssiCfg)

    private val _mode = MutableStateFlow(RadarMode.GPS_OFF)
    override val mode: StateFlow<RadarMode> = _mode.asStateFlow()

    private val _targets = MutableStateFlow<List<RadarTarget>>(emptyList())
    override val targets: StateFlow<List<RadarTarget>> = _targets.asStateFlow()

    private data class Entry(
        val originId: String,
        var lastMsgId: String,
        var lastSeenMs: Long,
        var rssiDbm: Int?,
        var payloadLoc: GeoLocation?,
        var bearingDeg: Double?,
        var approxRangeText: String?,
        var bucket: RangeBucket
    )

    private val lock = Any()
    private val byOrigin = linkedMapOf<String, Entry>()
    private var myLoc: GeoLocation? = null

    override fun setMode(mode: RadarMode) {
        _mode.value = mode
        // 모드 바뀌면 GPS_ON일 때 bearing 재계산 시도
        if (mode == RadarMode.GPS_ON) {
            val loc = myLoc
            if (loc != null) {
                synchronized(lock) {
                    byOrigin.values.forEach { e ->
                        e.bearingDeg = e.payloadLoc?.let { bearingDeg(loc, it) }
                    }
                    publishLocked()
                }
            }
        } else {
            synchronized(lock) {
                byOrigin.values.forEach { it.bearingDeg = null }
                publishLocked()
            }
        }
    }

    override fun onIncomingSos(
        originId: String,
        msgId: String,
        rssiDbm: Int?,
        payloadLocation: GeoLocation?,
        myLocation: GeoLocation?
    ) {
        val now = System.currentTimeMillis()
        val effectiveMyLoc = myLocation ?: myLoc

        synchronized(lock) {
            cleanupLocked(now)

            val estimate = estimator.update(originId, rssiDbm, now)
            val bucketFromRssi = estimate.bucket
            val approxFromRssi = estimate.displayRange?.removePrefix("약 ") // "약 20~40m" -> "20~40m"

            val (finalBucket, finalApprox, finalBearing) =
                if (_mode.value == RadarMode.GPS_ON && effectiveMyLoc != null && payloadLocation != null) {
                    val d = haversineMeters(effectiveMyLoc, payloadLocation)
                    val b = bucketByMeters(d)
                    val t = "${d.roundToInt()}m"
                    Triple(b, t, bearingDeg(effectiveMyLoc, payloadLocation))
                } else {
                    Triple(bucketFromRssi, approxFromRssi, null)
                }

            val entry = byOrigin[originId]
            if (entry == null) {
                byOrigin[originId] = Entry(
                    originId = originId,
                    lastMsgId = msgId,
                    lastSeenMs = now,
                    rssiDbm = rssiDbm,
                    payloadLoc = payloadLocation,
                    bearingDeg = finalBearing,
                    approxRangeText = finalApprox,
                    bucket = finalBucket
                )
            } else {
                entry.lastMsgId = msgId
                entry.lastSeenMs = now
                entry.rssiDbm = rssiDbm
                if (payloadLocation != null) entry.payloadLoc = payloadLocation
                entry.bearingDeg = finalBearing
                entry.approxRangeText = finalApprox
                entry.bucket = finalBucket
            }

            publishLocked()
        }
    }

    override fun onMyLocationUpdated(myLocation: GeoLocation) {
        myLoc = myLocation
        if (_mode.value != RadarMode.GPS_ON) return

        synchronized(lock) {
            val now = System.currentTimeMillis()
            cleanupLocked(now)

            byOrigin.values.forEach { e ->
                e.bearingDeg = e.payloadLoc?.let { bearingDeg(myLocation, it) }
                // GPS_ON인데 payloadLoc 없으면 bearing 계산 불가 → 그대로 null
            }
            publishLocked()
        }
    }

    // -------------------- helpers --------------------

    private fun bucketByMeters(m: Double): RangeBucket = when {
        m < rssiCfg.nearMaxM -> RangeBucket.NEAR
        m < rssiCfg.midMaxM -> RangeBucket.MID
        else -> RangeBucket.FAR
    }

    private fun publishLocked() {
        _targets.value = byOrigin.values
            .sortedByDescending { it.lastSeenMs }
            .map { e ->
                RadarTarget(
                    key = e.originId,
                    lastSeenMs = e.lastSeenMs,
                    bucket = e.bucket,
                    approxRangeText = e.approxRangeText,
                    bearingDeg = e.bearingDeg,
                    hasGps = (e.payloadLoc != null)

                )
            }
    }

    private fun cleanupLocked(now: Long) {
        val it = byOrigin.entries.iterator()
        while (it.hasNext()) {
            val e = it.next().value
            if (now - e.lastSeenMs > keepMs) it.remove()
        }
    }

    private fun haversineMeters(a: GeoLocation, b: GeoLocation): Double {
        val R = 6371000.0
        val lat1 = Math.toRadians(a.lat)
        val lat2 = Math.toRadians(b.lat)
        val dLat = lat2 - lat1
        val dLon = Math.toRadians(b.lng - a.lng)
        val sinDLat = sin(dLat / 2)
        val sinDLon = sin(dLon / 2)
        val h = sinDLat * sinDLat + cos(lat1) * cos(lat2) * sinDLon * sinDLon
        return 2 * R * asin(sqrt(h)).coerceAtMost(1.0)
    }

    private fun bearingDeg(from: GeoLocation, to: GeoLocation): Double {
        val lat1 = Math.toRadians(from.lat)
        val lat2 = Math.toRadians(to.lat)
        val dLon = Math.toRadians(to.lng - from.lng)

        val y = sin(dLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)
        val brng = Math.toDegrees(atan2(y, x))
        return (brng + 360.0) % 360.0
    }
}