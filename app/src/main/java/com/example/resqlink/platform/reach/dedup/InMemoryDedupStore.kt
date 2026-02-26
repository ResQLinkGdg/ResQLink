package com.example.resqlink.platform.reach.dedup

class InMemoryDedupStore(
    private val ttlMs: Long = 5 * 60 * 1000L // 5분
) : DedupStore {

    private val lock = Any()
    private val store = mutableMapOf<String, Long>()

    override fun isDuplicate(msgId: String): Boolean = synchronized(lock) {
        cleanup()
        store.containsKey(msgId)
    }

    override fun mark(msgId: String) =synchronized(lock) {
        store[msgId] = System.currentTimeMillis() + ttlMs
    }

    private fun cleanup() {
        val now = System.currentTimeMillis()
        store.entries.removeIf { it.value < now }
    }
}
