package com.example.resqlink.data.store

import android.content.Context
import java.util.UUID

class IdentityStore(private val context: Context) {
    private val prefs = context.getSharedPreferences("resqlink_prefs", Context.MODE_PRIVATE)

    fun getMyId(): String {
        var id = prefs.getString("my_sender_id", null)
        if (id == null) {
            id = UUID.randomUUID().toString() // 처음 한 번 생성
            prefs.edit().putString("my_sender_id", id).apply()
        }
        return id
    }
}