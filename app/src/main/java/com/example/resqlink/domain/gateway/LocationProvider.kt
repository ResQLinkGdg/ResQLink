package com.example.resqlink.domain.gateway

interface LocationProvider {
    suspend fun getCurrentLocation(): GeoLocation?
}

data class GeoLocation(
    val lat: Double,
    val lng: Double
)
