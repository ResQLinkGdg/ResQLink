package com.example.resqlink.platform.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.resqlink.domain.gateway.GeoLocation
import com.example.resqlink.domain.gateway.LocationProvider
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AndroidLocationProvider(
    private val context: Context
) : LocationProvider {

    private val client = LocationServices
        .getFusedLocationProviderClient(context.applicationContext)

    override suspend fun getCurrentLocation(): GeoLocation? {
        // 1️⃣ 권한 체크
        val hasPermission =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) return null

        // 2️⃣ 마지막 위치 가져오기 (빠르고 안정적)
        return suspendCancellableCoroutine { cont ->
            client.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        cont.resume(
                            GeoLocation(
                                lat = location.latitude,
                                lng = location.longitude
                            )
                        )
                    } else {
                        cont.resume(null)
                    }
                }
                .addOnFailureListener {
                    cont.resume(null)
                }
        }
    }
}
