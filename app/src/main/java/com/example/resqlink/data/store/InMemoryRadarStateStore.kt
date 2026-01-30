package com.example.resqlink.data.store

import com.example.resqlink.domain.gateway.GeoLocation
import com.example.resqlink.domain.model.Range.RangeBucket
import com.example.resqlink.domain.model.Range.RssiRangeConfig
import com.example.resqlink.domain.model.radar.RadarMode
import com.example.resqlink.domain.model.radar.RadarTarget
import com.example.resqlink.domain.policy.ranging.RssiRangeEstimator
import com.google.android.gms.nearby.messages.Distance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

private data class CalcResult(
    val bucket: RangeBucket,
    val approxText: String?,
    val bearing: Double?,
    val distanceM: Double?
)
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
        var bucket: RangeBucket,
        var distanceM: Double?

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

            val res = if (_mode.value == RadarMode.GPS_ON && effectiveMyLoc != null && payloadLocation != null) {
                val d = calculateGpsDistance(effectiveMyLoc, payloadLocation)
                //  Triple 대신 CalcResult 사용
                CalcResult(
                    bucket = bucketByMeters(d),
                    approxText = "${d.roundToInt()}m",
                    bearing = bearingDeg(effectiveMyLoc, payloadLocation),
                    distanceM = d
                )
            } else {
                val estimate = estimator.update(originId, rssiDbm, now)
                CalcResult(
                    bucket = estimate.bucket,
                    approxText = estimate.displayRange?.removePrefix("약 "),
                    bearing = null,
                    distanceM = estimate.distanceM // RSSI로 계산된 거리도 넣어주면 좋음
                )
            }

            val entry = byOrigin[originId]
            if (entry == null) {
                byOrigin[originId] = Entry(
                    originId = originId,
                    lastMsgId = msgId,
                    lastSeenMs = now,
                    rssiDbm = rssiDbm,
                    payloadLoc = payloadLocation,
                    bearingDeg = res.bearing,
                    distanceM=res.distanceM,
                    approxRangeText = res.approxText,
                    bucket = res.bucket
                )
            } else {
                entry.lastMsgId = msgId
                entry.lastSeenMs = now
                entry.rssiDbm = rssiDbm
                if (payloadLocation != null) entry.payloadLoc = payloadLocation
                entry.bucket = res.bucket
                entry.approxRangeText = res.approxText
                entry.bearingDeg = res.bearing
                entry.distanceM = res.distanceM
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
                    hasGps = (e.payloadLoc != null),
                    distanceM = e.distanceM

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

    private fun calculateGpsDistance(my: GeoLocation, target: GeoLocation): Double {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(
            my.lat, my.lng,
            target.lat, target.lng,
            results
        )
        return results[0].toDouble() // 미터(m) 단위 결과 반환
    }
}