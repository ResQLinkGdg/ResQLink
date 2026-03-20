package com.example.resqlink.platform.logging

import android.content.Context
import android.util.Log
import com.example.resqlink.domain.gateway.RssiDistanceLogger
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets

/**
 * actualDistanceM, rssiDbm을 로컬 CSV 파일에 append.
 * 학습/검증용 데이터 수집.
 */
class FileRssiDistanceLogger(
    private val context: Context
) : RssiDistanceLogger {

    private val logFile: File
        get() = File(context.filesDir, FILE_NAME)

    override fun log(actualDistanceM: Double, rssiDbm: Int, timestampMs: Long) {
        try {
            val file = logFile
            if (!file.exists()) {
                file.writeText("actualDistanceM,rssiDbm,timestampMs\n", StandardCharsets.UTF_8)
            }
            val line = "$actualDistanceM,$rssiDbm,$timestampMs\n"
            FileOutputStream(file, true).use { fos ->
                fos.write(line.toByteArray(StandardCharsets.UTF_8))
            }
            Log.d("ResQLink_RssiLog", "logged: dist=${actualDistanceM}m, rssi=$rssiDbm")
        } catch (e: Exception) {
            Log.e("ResQLink_RssiLog", "write failed: ${e.message}")
        }
    }

    companion object {
        private const val FILE_NAME = "rssi_distance_log.csv"
    }
}
