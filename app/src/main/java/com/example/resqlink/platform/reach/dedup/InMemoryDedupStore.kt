package com.example.resqlink.platform.reach.dedup

class InMemoryDedupStore(
    private val ttlMs: Long = 5 * 60 * 1000L // 5분
) : DedupStore {

    private val store = mutableMapOf<String, Long>()

    override fun isDuplicate(msgId: String): Boolean {
        cleanup()
        return store.containsKey(msgId)
    }

    override fun mark(msgId: String) {
        store[msgId] = System.currentTimeMillis() + ttlMs
    }

    private fun cleanup() {
        val now = System.currentTimeMillis()
        store.entries.removeIf { it.value < now }
    }
}
