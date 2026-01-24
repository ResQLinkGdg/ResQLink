package com.example.resqlink.domain.policy

interface DotLayoutPolicy {
    fun angleDeg(originId: String): Float
    fun alphaFromAgeSec(ageSec: Int): Float
}