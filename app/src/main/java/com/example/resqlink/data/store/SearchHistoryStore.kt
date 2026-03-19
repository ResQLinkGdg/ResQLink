package com.example.resqlink.data.store

import android.content.Context
import org.json.JSONArray

class SearchHistoryStore(context: Context) {

    private val prefs = context.getSharedPreferences("resqlink_search_history", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_HISTORY = "search_history"
        private const val MAX_SIZE = 5
    }

    fun getHistory(): List<String> {
        val json = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        val array = JSONArray(json)
        return (0 until array.length()).map { array.getString(it) }
    }

    fun addQuery(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return

        val current = getHistory().toMutableList()
        current.remove(trimmed)
        current.add(0, trimmed)

        val limited = current.take(MAX_SIZE)
        val array = JSONArray(limited)
        prefs.edit().putString(KEY_HISTORY, array.toString()).apply()
    }

    fun clearHistory() {
        prefs.edit().remove(KEY_HISTORY).apply()
    }
}
