package com.example.resqlink

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.text.style.LineBreak
import androidx.lifecycle.lifecycleScope
import com.example.resqlink.data.store.InMemoryRadarStateStore
import com.example.resqlink.domain.gateway.LocationProvider
import com.example.resqlink.domain.gateway.Transport
import com.example.resqlink.domain.usecase.radar.ApplyIncomingSosUsecase
import com.example.resqlink.domain.usecase.radar.RefreshMyLocationUsecase
import com.example.resqlink.domain.usecase.radar.SetRadarModeUsecase
import com.example.resqlink.domain.usecase.reach.ReachControlUseCase
import com.example.resqlink.platform.location.AndroidLocationProvider
import com.example.resqlink.platform.reach.dedup.InMemoryDedupStore
import com.example.resqlink.platform.reach.protocol.MessageCodec
import com.example.resqlink.platform.reach.receiver.ReachReceiver
import com.example.resqlink.platform.transport.nearby.NearbyConfig
import com.example.resqlink.platform.transport.nearby.NearbyTransport
import com.example.resqlink.ui.feature_responder.RadarRoute
import com.example.resqlink.ui.feature_responder.RadarViewModelFactory
import com.google.android.gms.nearby.connection.Strategy


class MainActivity : ComponentActivity() {

    // ★ circular dependency 깨려고 lateinit + provider 사용
    private lateinit var transport: Transport

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mySenderId = NearbyConfig.deviceName()
        val codec = MessageCodec()
        val dedup = InMemoryDedupStore()
        val locationProvider = AndroidLocationProvider(this)

        val store = InMemoryRadarStateStore()

        val applyIncomingSos = ApplyIncomingSosUsecase(
            store = store,
            locationProvider = locationProvider
        )

        val receiver = ReachReceiver(
            transportProvider = { transport },
            codec = codec,
            dedup = dedup,
            mySenderId = mySenderId,
            applyIncomingSos = applyIncomingSos,
            scope = lifecycleScope
        )

        transport = NearbyTransport(
            context = this,
            callbacks = receiver,
            serviceId = NearbyConfig.SERVICE_ID,
            localEndpointName = NearbyConfig.deviceName(),
            strategy = Strategy.P2P_STAR
        )

        val reachControl = ReachControlUseCase(
            transport = transport,
            locationProvider = locationProvider,
            codec = codec,
            mySenderId = mySenderId
        )

        val factory = RadarViewModelFactory(
            store = store,
            reachControl = reachControl,
            setRadarMode = SetRadarModeUsecase(store),
            refreshMyLocation = RefreshMyLocationUsecase(  locationProvider, store)
        )

        setContent {
            RadarRoute(factory = factory)
        }
    }
}
