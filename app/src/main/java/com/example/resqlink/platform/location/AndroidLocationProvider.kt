package com.example.resqlink.platform.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.resqlink.domain.gateway.GeoLocation
import com.example.resqlink.domain.gateway.LocationProvider
import com.google.android.gms.location.Priority
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

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

        return suspendCancellableCoroutine { cont ->
            val cts = CancellationTokenSource()
            cont.invokeOnCancellation { cts.cancel() }

            // 2️⃣ getCurrentLocation: 캐시 없어도 새 위치 요청 (최대 ~15초 대기)
            client.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                cts.token
            )
                .addOnSuccessListener { location ->
                    if (location != null) {
                        cont.resume(
                            GeoLocation(
                                lat = location.latitude,
                                lng = location.longitude
                            )
                        )
                    } else {
                        // 3️⃣ null이면 lastLocation 캐시 폴백
                        tryLastLocation(cont)
                    }
                }
                .addOnFailureListener {
                    tryLastLocation(cont)
                }
        }
    }

    private fun tryLastLocation(cont: Continuation<GeoLocation?>) {
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
