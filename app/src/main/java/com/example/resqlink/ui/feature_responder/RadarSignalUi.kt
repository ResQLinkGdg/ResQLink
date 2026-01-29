package com.example.resqlink.ui.feature_responder

import androidx.appcompat.widget.DialogTitle
import com.example.resqlink.domain.model.Range.RangeBucket

data class RadarSignalUi(
    val key: String,                 // originId 또는 msgId 등 "고정 키"
    val bucket: RangeBucket,
<<<<<<< HEAD
    val distanceM: Double? = null,
=======
>>>>>>> c3c7fa588f6255b2cb07249899b5fd067c0b13e4
    val bearingDeg: Double? = null,  // GPS ON에서만 값이 들어옴
    val displayRange: String? = null, // "약 12~20m" 같은 표시 문자열(스토어에서 만들어서 넣기)
    val rssiDbm: Int? = null,
    val title: String?=null,
    val text: String?=null,
    val lastSeenMs: Long
)