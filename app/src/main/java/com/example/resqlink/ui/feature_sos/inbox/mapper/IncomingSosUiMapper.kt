package com.example.resqlink.ui.feature_sos.inbox.mapper

import com.example.resqlink.domain.model.sos.IncomingSosEvent
import com.example.resqlink.platform.reach.protocol.sos.SosSituation
import com.example.resqlink.platform.reach.protocol.sos.SosUrgency
import com.example.resqlink.ui.feature_sos.inbox.model.RiskLevel
import com.example.resqlink.ui.feature_sos.inbox.model.SosReportUiModel
import kotlin.math.abs

fun IncomingSosEvent.toUiModel(): SosReportUiModel {
    val minutesAgo =
        ((System.currentTimeMillis() - timestampMs) / 60_000).toInt()
            .coerceAtLeast(0)

    return SosReportUiModel(
        id = msgId,
        risk = urgency.toRiskLevel(),
        category = situation.displayName(),
        title = hint ?: "상황 설명 없음",
        minutesAgo = minutesAgo,
        distanceM = estimateDistanceM(),
        signalText = rssiDbm.toSignalText()
    )
}

private fun SosUrgency.toRiskLevel(): RiskLevel =
    when (this) {
        SosUrgency.HIGH -> RiskLevel.HIGH
        SosUrgency.MEDIUM -> RiskLevel.MID
        SosUrgency.LOW -> RiskLevel.LOW
    }

private fun SosSituation.displayName(): String =
    when (this) {
        SosSituation.TRAPPED -> "매몰"
        SosSituation.BLEEDING -> "출혈"
        SosSituation.CARDIAC -> "심정지"
        SosSituation.ISOLATED -> "고립"
        SosSituation.FIRE -> "화재/연기"
        SosSituation.OTHER -> "기타"
    }

private fun IncomingSosEvent.estimateDistanceM(): Int? {

    // 1순위: GPS 좌표가 있다면 정밀 계산 실행
    if (payloadLocation != null && myLocation != null) {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(
            myLocation.lat, myLocation.lng,      // 내 위치
            payloadLocation.lat, payloadLocation.lng, // 상대 위치
            results
        )
        return results[0].toInt() // 미터(m) 단위 결과 반환
    }

    // 2순위: 좌표가 없으면 기존 RSSI 추정 로직 사용
    val rssi = rssiDbm ?: hops.lastOrNull()?.rssi ?: return null
    return when {
        rssi >= -60 -> 10
        rssi >= -70 -> 30
        rssi >= -80 -> 70
        else -> 150
    }
}

private fun Int?.toSignalText(): String =
    this?.let { "RSSI ${abs(it)} dBm" } ?: "신호 미확인"
