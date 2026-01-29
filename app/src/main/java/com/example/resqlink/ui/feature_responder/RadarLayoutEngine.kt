package com.example.resqlink.ui.feature_responder

import com.example.resqlink.domain.model.Range.RangeBucket
import com.example.resqlink.domain.model.radar.RadarMode
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * 레이더 점 배치 엔진
 * - GPS_ON : bearing 있는 점만 링 위(각도 기반)
 * - GPS_OFF: 방향 없는 점을 "세로축" 기반 거리대역으로만 배치
 */
class RadarLayoutEngine(
    private val cfg: Config = Config()
) {
    data class Config(
        // 링 반지름 비율(화면 짧은 변 기준)
        val nearFrac: Float = 0.22f,
        val midFrac: Float  = 0.42f,
        val farFrac: Float  = 0.62f,

        // 점 크기/겹침 방지
        val dotRadiusPx: Float = 9f,
        val minSeparationPx: Float = 22f,

        // GPS_ON 겹침 해결용 각도 스텝/시도 횟수
        val angleStepDeg: Double = 14.0,
        val maxTries: Int = 30,

        // 링 위 점이 너무 딱딱해보이면 약간의 반지름 지터(픽셀)
        val ringJitterPx: Float = 4f,

        // GPS_OFF(방향 없음) 레이아웃: 세로축 + 좌우 슬롯
        val offModeAnchorYFrac: Float = 0.86f, // YOU가 화면 아래쪽(0.86 * height)
        val slotSpacingPx: Float = 20f,        // 같은 버킷 내 점 간 기본 간격
        val maxXSpreadPx: Float = 90f,         // 좌우로 최대 퍼지는 범위
        val offJitterPx: Float = 3f            // 미세 지터(고정 랜덤)
    )

    companion object {
        // 클래스 내부에서 상수를 쓰고 싶다면 반드시 companion object 안에!
        const val MAX_DISTANCE = 200.0
    }
    private fun calculateRadius(radii: Radii, signal: RadarSignalUi): Float {
        return if (signal.distanceM != null) {
            // ⭐ 버킷 고정값이 아니라, 실제 거리에 비례해서 반지름 결정
            val fraction = (signal.distanceM / MAX_DISTANCE).coerceIn(0.0, 1.0).toFloat()
            radii.far * fraction
        } else {
            // GPS가 없으면 기존처럼 버킷 반지름(0.22, 0.42 등) 사용
            radiusForBucket(radii, signal.bucket)
        }
    }


    fun layout(
        signals: List<RadarSignalUi>,
        widthPx: Float,
        heightPx: Float,
        mode: RadarMode
    ): List<PlacedDot> {
        val minDim = min(widthPx, heightPx)

        // 중심점: GPS_ON은 화면 중앙, GPS_OFF는 아래쪽(너가 하단에 있는 UI)
        val cx = widthPx / 2f
        val cy = if (mode == RadarMode.GPS_ON) heightPx / 2f else heightPx * cfg.offModeAnchorYFrac

        val radii = Radii(
            near = minDim * cfg.nearFrac,
            mid  = minDim * cfg.midFrac,
            far  = minDim * cfg.farFrac
        )

        val placed = mutableListOf<PlacedDot>()

        if (mode == RadarMode.GPS_ON) {
            signals.filter { it.bearingDeg != null && it.bucket != RangeBucket.UNKNOWN }.forEach { s ->
                val r0 = calculateRadius(radii, s)
                val (x, y) = placeOnRingAvoidingOverlap(s.key, cx, cy, r0, s.bearingDeg!!, placed)
                placed += PlacedDot(s.key, s.bucket, x, y)
            }
        } else {
            signals.filter { it.bucket != RangeBucket.UNKNOWN }.groupBy { it.bucket }.forEach { (b, group) ->
                val sortedGroup = group.sortedBy { it.key.hashCode() }
                val n = sortedGroup.size
                sortedGroup.withIndex().forEach { (i, s) ->
                    val r0 = calculateRadius(radii, s)
                    val yBase = cy - r0
                    val currentSpacing = if (n <= 1) 0f else min(cfg.slotSpacingPx, (cfg.maxXSpreadPx * 2) / (n + 1))
                    var x = cx + (i - (n - 1) / 2f) * currentSpacing + jitterSignedPx(s.key, "x", cfg.offJitterPx)
                    var y = yBase + jitterSignedPx(s.key, "y", cfg.offJitterPx)
                    placed += PlacedDot(s.key, s.bucket, x, y)
                }
            }
        }
        return placed
    }

    // ----------------- 내부 유틸 -----------------

    private data class Radii(val near: Float, val mid: Float, val far: Float)

    private fun radiusForBucket(r: Radii, bucket: RangeBucket): Float = when (bucket) {
        RangeBucket.NEAR -> r.near
        RangeBucket.MID  -> r.mid
        RangeBucket.FAR  -> r.far
        RangeBucket.UNKNOWN -> r.far
    }

    private fun placeOnRingAvoidingOverlap(
        key: String,
        cx: Float, cy: Float,
        r0: Float,
        baseAngleDeg: Double,
        alreadyPlaced: List<PlacedDot>
    ): Pair<Float, Float> {
        // 반지름 지터(고정)
        val r = r0 + jitterSignedPx(key, "r", cfg.ringJitterPx)

        // 같은 bearing이 몰릴 때 각도를 조금씩 밀어 겹침 회피
        for (t in 0 until cfg.maxTries) {
            val offset = ((t + 1) / 2) * cfg.angleStepDeg * if (t % 2 == 0) 1 else -1
            val ang = (baseAngleDeg + offset + 360.0) % 360.0
            val (x, y) = polarToXY(cx, cy, r, ang)

            if (!collides(x, y, alreadyPlaced)) return x to y
        }

        // 다 실패하면 그냥 base에 찍기
        return polarToXY(cx, cy, r, baseAngleDeg)
    }

    private fun polarToXY(cx: Float, cy: Float, radius: Float, bearingDeg: Double): Pair<Float, Float> {
        // bearing: 북=0, 시계방향 증가 가정
        val rad = Math.toRadians(bearingDeg)
        val x = cx + (radius * sin(rad)).toFloat()
        val y = cy - (radius * cos(rad)).toFloat() // 화면 y축 아래로 증가라서 -
        return x to y
    }

    private fun collides(x: Float, y: Float, placed: List<PlacedDot>): Boolean {
        val minD = max(cfg.minSeparationPx, cfg.dotRadiusPx * 2f)
        val minD2 = minD * minD
        for (p in placed) {
            val dx = x - p.x
            val dy = y - p.y
            if (dx * dx + dy * dy < minD2) return true
        }
        return false
    }

    private fun stableHash(s: String): Int = s.hashCode()

    private fun jitterSignedPx(key: String, salt: String, amp: Float): Float {
        // key+salt 기반 고정 랜덤 [-amp, +amp]
        val h = stableHash("$key|$salt")
        val u = (h and 0x7fffffff) / Int.MAX_VALUE.toFloat() // 0..1
        return (u * 2f - 1f) * amp
    }
}