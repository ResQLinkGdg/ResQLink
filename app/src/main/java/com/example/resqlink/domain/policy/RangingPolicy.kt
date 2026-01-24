package com.example.resqlink.domain.policy

import com.example.resqlink.domain.model.NodeSummary
import com.example.resqlink.domain.model.ProximityHint

interface RangingPolicy {
    fun hintFor(node: NodeSummary, now: Long): ProximityHint
}