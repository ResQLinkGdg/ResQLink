package com.example.resqlink.domain.usecase.radar

import com.example.resqlink.data.store.RadarStateStore
import com.example.resqlink.domain.gateway.LocationProvider

class RefreshMyLocationUsecase(
    private val locationProvider: LocationProvider,
    private val store: RadarStateStore
) {
    suspend operator fun invoke() {
        val myLoc = locationProvider.getCurrentLocation() ?: return
        store.onMyLocationUpdated(myLoc)
    }
}