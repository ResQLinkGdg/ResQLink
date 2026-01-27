package com.example.resqlink.platform.transport.nearby

object NearbyConfig {
    const val SERVICE_ID = "com.example.resqlink.REACH"

    fun deviceName(): String =
        android.os.Build.MODEL ?: "Android"
}
