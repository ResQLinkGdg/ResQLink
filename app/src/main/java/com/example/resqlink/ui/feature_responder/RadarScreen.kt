package com.example.resqlink.ui.feature_responder

import android.R.attr.shape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.border
/**
 * 네 프로젝트에 이미 있는 모델을 쓰는 걸 전제로 했고,
 * (필드가 다르면) 아래 RadarSignalUi에 lastSeenMs 같은 것만 추가해주면 됨.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadarScreen(
    uiState: RadarUiState,
    onToggleMode: (com.example.resqlink.domain.model.radar.RadarMode) -> Unit,
    onSelectKey: (String) -> Unit,
    onDismissSelection: () -> Unit,
    modifier: Modifier = Modifier
) {
    val signals = uiState.signals

    val nearCount = remember(signals) { signals.count { it.bucket == com.example.resqlink.domain.model.Range.RangeBucket.NEAR } }
    val midCount = remember(signals) { signals.count { it.bucket == com.example.resqlink.domain.model.Range.RangeBucket.MID } }
    val farCount = remember(signals) { signals.count { it.bucket == com.example.resqlink.domain.model.Range.RangeBucket.FAR } }
    val unknownCount = remember(signals) { signals.count { it.bucket == com.example.resqlink.domain.model.Range.RangeBucket.UNKNOWN } }

    val sheetSignal = remember(uiState.selectedKey, signals) {
        signals.firstOrNull { it.key == uiState.selectedKey }
    }

    Column(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .padding(top = 10.dp)
    ) {
        // ===== 상단 타이틀 영역(원하면 너 앱 상단바에 맞게 빼도 됨) =====
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("구조대 상황 분포", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(2.dp))
                Text("발견된 신호: ${signals.size}개", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // 활성 뱃지 느낌
            AssistChip(
                onClick = {},
                label = { Text("활성") },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                border = null
            )
        }

        Spacer(Modifier.height(12.dp))

        // ===== 레이더 카드 =====
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("레이더 분포도", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(2.dp))
                        val subtitle = if (uiState.mode == com.example.resqlink.domain.model.radar.RadarMode.GPS_ON)
                            "거리별 신호 분포 (최근순)"
                        else
                            "신호 세기 기반 (방향 정보 없음)"
                        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    val gpsOn = uiState.mode == com.example.resqlink.domain.model.radar.RadarMode.GPS_ON
                    OutlinedButton(
                        onClick = {
                            onToggleMode(
                                if (gpsOn) com.example.resqlink.domain.model.radar.RadarMode.GPS_OFF
                                else com.example.resqlink.domain.model.radar.RadarMode.GPS_ON
                            )
                        },
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (gpsOn) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, if (gpsOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                    ) {
                        Text("GPS", color = if (gpsOn) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)
                    }
                }

                Spacer(Modifier.height(14.dp))

                // ===== 레이더 본체 =====
                RadarChartCardBody(
                    mode = uiState.mode,
                    signals = signals,
                    selectedKey = uiState.selectedKey,
                    onSelectKey = onSelectKey
                )

                Spacer(Modifier.height(14.dp))

                // ===== 범례 =====
                LegendRow()

                Spacer(Modifier.height(10.dp))
                Text(
                    "* 불투명도가 높을수록 신뢰도가 높습니다",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // ===== 거리별 요약 =====
        Text("거리별 요약", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(10.dp))

        SummaryGrid(
            nearCount = nearCount,
            midCount = midCount,
            farCount = farCount,
            unknownCount = unknownCount
        )

        Spacer(Modifier.height(12.dp))
    }

    // ===== 선택 시 바텀시트(원하면 유지) =====
    if (sheetSignal != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = onDismissSelection,
            sheetState = sheetState
        ) {
            SignalDetailSheet(sheetSignal)
        }
    }
}

/* ----------------------------- Radar Body ----------------------------- */

@Composable
private fun RadarChartCardBody(
    mode: com.example.resqlink.domain.model.radar.RadarMode,
    signals: List<RadarSignalUi>,
    selectedKey: String?,
    onSelectKey: (String) -> Unit
) {
    // 카드 안에서 “원형 크게” 보여주기 위해 폭 기준으로 사이즈 잡음
    BoxWithConstraints(
        Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        val size = minOf(maxWidth, 360.dp)
        Box(
            Modifier
                .size(size)
                .padding(vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            if (mode == com.example.resqlink.domain.model.radar.RadarMode.GPS_ON) {
                RadarCircleChart(
                    size = size,
                    signals = signals,
                    selectedKey = selectedKey,
                    onSelectKey = onSelectKey
                )
            } else {
                RadarAxisChart(
                    size = size,
                    signals = signals,
                    selectedKey = selectedKey,
                    onSelectKey = onSelectKey
                )
            }
        }
    }
}

/**
 * GPS ON: bearing 있는 애들은 동심원 위에 각도대로,
 * UNKNOWN/방향없음은 바깥(UNKNOWN 영역)으로 분산.
 */
@Composable
private fun RadarCircleChart(
    size: Dp,
    signals: List<RadarSignalUi>,
    selectedKey: String?,
    onSelectKey: (String) -> Unit
) {
    val density = LocalDensity.current
    val sizePx = with(density) { size.toPx() }

    val cx = sizePx / 2f
    val cy = sizePx / 2f

    val nearR = sizePx * 0.22f
    val midR  = sizePx * 0.45f
    val farR  = sizePx * 0.70f

    val dashed = remember { PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f) }
    val ringStroke = remember { Stroke(width = 3f, pathEffect = dashed) }

    val now = remember { System.currentTimeMillis() }

    val known = remember(signals) { signals.filter { it.bucket != com.example.resqlink.domain.model.Range.RangeBucket.UNKNOWN } }
    val unknown = remember(signals) { signals.filter { it.bucket == com.example.resqlink.domain.model.Range.RangeBucket.UNKNOWN } }

    // 배치: bearing 있으면 각도대로, 없으면 “원형에서 임의 각도(고정)”로
    fun radiusFor(bucket: com.example.resqlink.domain.model.Range.RangeBucket): Float = when (bucket) {
        com.example.resqlink.domain.model.Range.RangeBucket.NEAR -> nearR
        com.example.resqlink.domain.model.Range.RangeBucket.MID -> midR
        com.example.resqlink.domain.model.Range.RangeBucket.FAR -> farR
        com.example.resqlink.domain.model.Range.RangeBucket.UNKNOWN -> farR
    }

    Box(Modifier.fillMaxSize()) {
        // 링 + 라벨
        androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
            // 배경은 카드 내부니까 투명 그대로
            drawCircle(
                color = Color(0xFFE9EEF6),
                radius = farR + 18f,
                center = Offset(cx, cy),
                style = Stroke(width = 3f)
            )
            drawCircle(color = Color(0xFFB9C3D6), radius = farR, center = Offset(cx, cy), style = ringStroke)
            drawCircle(color = Color(0xFFB9C3D6), radius = midR, center = Offset(cx, cy), style = ringStroke)
            drawCircle(color = Color(0xFFB9C3D6), radius = nearR, center = Offset(cx, cy), style = ringStroke)

            // YOU (중앙)
            drawCircle(color = Color(0xFF2F6BFF), radius = 10f, center = Offset(cx, cy))
        }

        // 링 라벨(대략 위쪽)
        RingLabel(text = "FAR (150m+)", xPx = cx, yPx = cy - farR, offsetY = (-18).dp)
        RingLabel(text = "MID (50-150m)", xPx = cx, yPx = cy - midR, offsetY = (-18).dp)
        RingLabel(text = "NEAR (0-50m)", xPx = cx, yPx = cy - nearR, offsetY = (-18).dp)
        RingLabel(text = "UNKNOWN", xPx = cx - farR * 0.85f, yPx = cy - farR * 0.85f, offsetY = 0.dp, alignStart = true)

        // 점 찍기
        val dotSize = 14.dp
        val dotPx = with(density) { dotSize.toPx() }

        // known: bearing 있으면 그 각도, 없으면 key 기반 각도
        known.forEach { s ->
            val r = radiusFor(s.bucket)
            val angle = (s.bearingDeg ?: stableAngleDeg(s.key)).toRadians()
            val x = cx + (r * cos(angle)).toFloat()
            val y = cy - (r * sin(angle)).toFloat()

            RadarDot(
                xPx = x,
                yPx = y,
                size = dotSize,
                isSelected = (s.key == selectedKey),
                color = ageColor(now, s),
                onClick = { onSelectKey(s.key) }
            )
        }

        // unknown: 바깥쪽에 두기
        unknown.forEach { s ->
            val angle = stableAngleDeg("U:${s.key}").toRadians()
            val r = farR + 28f
            val x = cx + (r * cos(angle)).toFloat()
            val y = cy - (r * sin(angle)).toFloat()

            RadarDot(
                xPx = x,
                yPx = y,
                size = dotSize,
                isSelected = (s.key == selectedKey),
                color = ageColor(now, s),
                onClick = { onSelectKey(s.key) }
            )
        }
    }
}

/**
 * GPS OFF: 방향 없으니 “세로축 + 거리대역 위치만” 배치
 * (레퍼런스처럼 YOU가 아래, 점들이 각 대역에서 좌우로 분산)
 */
@Composable
private fun RadarAxisChart(
    size: Dp,
    signals: List<RadarSignalUi>,
    selectedKey: String?,
    onSelectKey: (String) -> Unit
) {
    val density = LocalDensity.current
    val sizePx = with(density) { size.toPx() }
    val now = remember { System.currentTimeMillis() }

    val cx = sizePx / 2f
    val topY = sizePx * 0.10f
    val bottomY = sizePx * 0.88f

    fun laneY(bucket: com.example.resqlink.domain.model.Range.RangeBucket): Float = when (bucket) {
        com.example.resqlink.domain.model.Range.RangeBucket.FAR -> sizePx * 0.18f
        com.example.resqlink.domain.model.Range.RangeBucket.MID -> sizePx * 0.42f
        com.example.resqlink.domain.model.Range.RangeBucket.NEAR -> sizePx * 0.68f
        com.example.resqlink.domain.model.Range.RangeBucket.UNKNOWN -> sizePx * 0.18f
    }

    fun laneLabel(bucket: com.example.resqlink.domain.model.Range.RangeBucket): String = when (bucket) {
        com.example.resqlink.domain.model.Range.RangeBucket.NEAR -> "NEAR\n0-50m"
        com.example.resqlink.domain.model.Range.RangeBucket.MID -> "MID\n50-150m"
        com.example.resqlink.domain.model.Range.RangeBucket.FAR -> "FAR\n150m+"
        com.example.resqlink.domain.model.Range.RangeBucket.UNKNOWN -> "UNKNOWN\n범위 밖"
    }

    Box(Modifier.fillMaxSize()) {
        // 라인/표식
        androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
            // 세로축
            drawLine(
                color = Color(0xFF2F6BFF),
                start = Offset(cx, topY),
                end = Offset(cx, bottomY),
                strokeWidth = 6f
            )
            // YOU
            drawCircle(color = Color(0xFF2F6BFF), radius = 12f, center = Offset(cx, bottomY))
        }

        // 라벨
        AxisLabel(text = "FAR\n150m+", xPx = cx + 80f, yPx = laneY(com.example.resqlink.domain.model.Range.RangeBucket.FAR))
        AxisLabel(text = "MID\n50-150m", xPx = cx + 80f, yPx = laneY(com.example.resqlink.domain.model.Range.RangeBucket.MID))
        AxisLabel(text = "NEAR\n0-50m", xPx = cx + 80f, yPx = laneY(com.example.resqlink.domain.model.Range.RangeBucket.NEAR))
        AxisLabel(text = "UNKNOWN\n(범위 밖)", xPx = cx - 120f, yPx = laneY(com.example.resqlink.domain.model.Range.RangeBucket.UNKNOWN), alignStart = true)

        // YOU 텍스트
        Text(
            "YOU",
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFF2F6BFF),
            modifier = Modifier.offset { IntOffset((cx - 14f).roundToInt(), (bottomY + 16f).roundToInt()) }
        )

        // 점들: 각 lane에서 좌우 분산
        val dotSize = 14.dp
        val maxSpread = sizePx * 0.28f

        signals.forEach { s ->
            val y = laneY(s.bucket)

            // key 해시로 안정적인 x 오프셋(방향 없음 느낌)
            val h = stableHash(s.key)
            val spread = ((h % 9) - 4) / 4f // -1 ~ +1
            val x = cx + spread * maxSpread

            RadarDot(
                xPx = x,
                yPx = y,
                size = dotSize,
                isSelected = (s.key == selectedKey),
                color = ageColor(now, s),
                onClick = { onSelectKey(s.key) }
            )
        }
    }
}

/* ----------------------------- Legend & Summary ----------------------------- */

@Composable
private fun LegendRow() {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendItem(color = Color(0xFFFF4D4D), text = "최신 (0-5분)")
        LegendItem(color = Color(0xFFFF9F1A), text = "중간 (5-15분)")
        LegendItem(color = Color(0xFF2ECC71), text = "오래됨 (15분+)")
    }
}

@Composable
private fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SummaryGrid(
    nearCount: Int,
    midCount: Int,
    farCount: Int,
    unknownCount: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SummaryCard(
                title = "NEAR",
                subtitle = "0-50m",
                count = nearCount,
                border = Color(0xFF7BD389),
                tint = Color(0x147BD389),
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                title = "MID",
                subtitle = "50-150m",
                count = midCount,
                border = Color(0xFFFFC857),
                tint = Color(0x14FFC857),
                modifier = Modifier.weight(1f)
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SummaryCard(
                title = "FAR",
                subtitle = "150m+",
                count = farCount,
                border = Color(0xFFB0B7C3),
                tint = Color(0x14B0B7C3),
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                title = "UNKNOWN",
                subtitle = "범위 밖",
                count = unknownCount,
                border = Color(0xFFB085F5),
                tint = Color(0x14B085F5),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    subtitle: String,
    count: Int,
    border: Color,
    tint: Color,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(18.dp)

    ElevatedCard(
        modifier = modifier
            .height(92.dp)
            .border(width = 2.dp,
                color = border,
                shape = shape),
        shape = shape,
        colors = CardDefaults.elevatedCardColors(containerColor = tint)
    ) {
        Row(
            Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                "$count",
                style = MaterialTheme.typography.headlineMedium,
                color = border
            )
        }
    }
}

/* ----------------------------- Bottom Sheet ----------------------------- */

@Composable
private fun SignalDetailSheet(signal: RadarSignalUi) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(signal.title ?: "SOS Signal", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))

        Text("key: ${signal.key}")
        Text("bucket: ${signal.bucket}")
        Text("rssi: ${signal.rssiDbm ?: "N/A"} dBm")
        Text("range: ${signal.displayRange ?: "N/A"}")
        Text("bearing: ${signal.bearingDeg?.let { "${"%.1f".format(it)}°" } ?: "N/A"}")

        signal.text?.takeIf { it.isNotBlank() }?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, maxLines = 4, overflow = TextOverflow.Ellipsis)
        }

        Spacer(Modifier.height(18.dp))
    }
}

/* ----------------------------- Small UI helpers ----------------------------- */

@Composable
private fun RadarDot(
    xPx: Float,
    yPx: Float,
    size: Dp,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    val density = LocalDensity.current
    val px = with(density) { size.toPx() }

    Box(
        Modifier
            .offset { IntOffset((xPx - px / 2f).roundToInt(), (yPx - px / 2f).roundToInt()) }
            .size(size)
            .background(color, CircleShape)
            .border(
                width = if (isSelected) 3.dp else 2.dp,
                color = Color.White.copy(alpha = if (isSelected) 0.95f else 0.8f),
                shape = CircleShape
            )
            .clickable(onClick = onClick)
    )
}

@Composable
private fun RingLabel(
    text: String,
    xPx: Float,
    yPx: Float,
    offsetY: Dp,
    alignStart: Boolean = false
) {
    val density = LocalDensity.current
    val xDp = with(density) { xPx.toDp() }
    val yDp = with(density) { yPx.toDp() }

    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .offset(x = if (alignStart) xDp else (xDp - 36.dp), y = yDp + offsetY)
    )
}

@Composable
private fun AxisLabel(
    text: String,
    xPx: Float,
    yPx: Float,
    alignStart: Boolean = false
) {
    val density = LocalDensity.current
    val xDp = with(density) { xPx.toDp() }
    val yDp = with(density) { yPx.toDp() }

    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.offset(x = if (alignStart) xDp else (xDp - 22.dp), y = yDp - 16.dp)
    )
}

/* ----------------------------- Age coloring ----------------------------- */

private fun ageColor(nowMs: Long, s: RadarSignalUi): Color {
    // lastSeenMs가 없다면(아직 모델에 없으면) 기본 RECENT 처리
    val last = s.lastSeenMs ?: nowMs
    val ageMin = ((nowMs - last).coerceAtLeast(0L) / 60000L).toInt()

    return when {
        ageMin <= 5 -> Color(0xFFFF4D4D) // RECENT
        ageMin <= 15 -> Color(0xFFFF9F1A) // MID
        else -> Color(0xFF2ECC71) // OLD
    }.copy(alpha = when {
        ageMin <= 5 -> 0.95f
        ageMin <= 15 -> 0.80f
        else -> 0.55f
    })
}

/* ----------------------------- Stable helpers ----------------------------- */

private fun stableHash(key: String): Int = key.fold(0) { acc, c -> acc * 31 + c.code }

private fun stableAngleDeg(key: String): Double {
    val h = stableHash(key)
    val deg = (h % 360 + 360) % 360
    return deg.toDouble()
}

private fun Double.toRadians(): Double = this * PI / 180.0
