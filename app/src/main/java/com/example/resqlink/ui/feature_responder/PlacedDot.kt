package com.example.resqlink.ui.feature_responder

import com.example.resqlink.domain.model.Range.RangeBucket

data class PlacedDot(
    val key: String,
    val bucket: RangeBucket,
    val x: Float,
    val y: Float
)