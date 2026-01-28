package com.example.resqlink.domain.policy.ranging

import com.example.resqlink.domain.model.Range.RangeBucket
import com.example.resqlink.domain.model.Range.RangeEstimate
import com.example.resqlink.domain.model.Range.RssiRangeConfig
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

class RssiRangeEstimator(private val cfg: RssiRangeConfig = RssiRangeConfig()) {

    private data class State(
        var ema: Double,
        var varEma: Double,
        var lastTs: Long
    )

    private val states = mutableMapOf<String, State>()

    fun update(key: String, rssiDbm: Int?, nowMs: Long): RangeEstimate {
        cleanup(nowMs)

        val rssi = rssiDbm ?: return RangeEstimate(
            distanceM = null,
            bucket = RangeBucket.UNKNOWN,
            confidence = 0.0,
            displayRange = null
        )

        val st = states[key]
        val (ema, conf) = if (st == null) {
            val s = State(ema = rssi.toDouble(), varEma = 0.0, lastTs = nowMs)
            states[key] = s
            s.ema to 0.6
        } else {
            val prev = st.ema
            val newEma = cfg.alpha * rssi + (1.0 - cfg.alpha) * prev

            val delta = abs(newEma - prev)
            st.varEma = cfg.varAlpha * delta + (1.0 - cfg.varAlpha) * st.varEma

            st.ema = newEma
            st.lastTs = nowMs

            val c = (1.0 - (st.varEma / cfg.varScaleDb)).coerceIn(0.05, 0.95)
            newEma to c
        }

        val distance = estimateDistanceMeters(ema)
        val bucket = bucketByMeters(distance)
        val display = displayRangeText(distance, bucket)

        return RangeEstimate(distance, bucket, conf, display)
    }

    fun getSmoothedRssi(key: String): Double? = states[key]?.ema

    private fun estimateDistanceMeters(smoothedRssiDbm: Double): Double {
        //  로그거리(path-loss) 모델
        // d = 10 ^ ((TxPower - RSSI) / (10 * n))
        val exp = (cfg.txPowerAt1mDbm - smoothedRssiDbm) / (10.0 * cfg.pathLossN)
        val d = 10.0.pow(exp)
        return d.coerceIn(cfg.minDistanceM, cfg.maxDistanceM)
    }

    private fun bucketByMeters(m: Double): RangeBucket = when {
        m < cfg.nearMaxM -> RangeBucket.NEAR
        m < cfg.midMaxM  -> RangeBucket.MID
        else -> RangeBucket.FAR
    }

    private fun displayRangeText(m: Double, bucket: RangeBucket): String {
        val band = when (bucket) {
            RangeBucket.NEAR -> cfg.bandNear
            RangeBucket.MID  -> cfg.bandMid
            RangeBucket.FAR  -> cfg.bandFar
            RangeBucket.UNKNOWN -> return "범위 밖"
        }

        val lo = max(0.0, m * band.loMul)
        val hi = min(cfg.maxDistanceM, m * band.hiMul)
        return "약 ${lo.roundToInt()}~${hi.roundToInt()}m"
    }

    private fun cleanup(nowMs: Long) {
        val it = states.entries.iterator()
        while (it.hasNext()) {
            val e = it.next()
            if (nowMs - e.value.lastTs > cfg.expireMs) it.remove()
        }
    }
}