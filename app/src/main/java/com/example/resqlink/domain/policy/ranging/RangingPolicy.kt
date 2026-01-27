package com.example.resqlink.domain.policy.ranging

import com.example.resqlink.domain.model.NodeSummary
import com.example.resqlink.domain.model.proximity.ProximityHint

interface RangingPolicy {
    fun hintFor(node: NodeSummary, now: Long): ProximityHint
}