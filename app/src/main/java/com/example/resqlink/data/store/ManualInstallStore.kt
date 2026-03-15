package com.example.resqlink.data.store

import android.content.Context

class ManualInstallStore(context: Context) {

    private val prefs = context.getSharedPreferences("resqlink_manual", Context.MODE_PRIVATE)

    fun isInstalled(): Boolean = prefs.getBoolean(KEY_INSTALLED, false)

    fun markInstalled() {
        prefs.edit().putBoolean(KEY_INSTALLED, true).apply()
    }

    fun clear() {
        prefs.edit().remove(KEY_INSTALLED).apply()
    }

    companion object {
        private const val KEY_INSTALLED = "manual_installed"
    }
}
