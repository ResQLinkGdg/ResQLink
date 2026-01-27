package com.example.resqlink.platform.reach.dedup

interface DedupStore {
    fun isDuplicate(msgId: String): Boolean
    fun mark(msgId: String)
}
