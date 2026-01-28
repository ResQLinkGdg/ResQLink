package com.example.resqlink

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.resqlink.data.store.InMemoryRadarStateStore
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
import com.example.resqlink.ui.AppRoute
import com.example.resqlink.ui.common.component.BottomNavBar
import com.example.resqlink.ui.common.model.BottomTab
import com.example.resqlink.ui.feature_responder.RadarRoute
import com.example.resqlink.ui.feature_responder.RadarViewModelFactory
import com.example.resqlink.ui.feature_sos.compose.SosComposeRoute
import com.example.resqlink.ui.feature_sos.inbox.SosInboxRoute
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
            val navController = rememberNavController()

            // 현재 route → BottomTab 매핑
            val currentBackStack by navController.currentBackStackEntryAsState()
            val currentRoute = currentBackStack?.destination?.route

            val currentTab = when (currentRoute) {
                AppRoute.SosInbox.route -> BottomTab.SOS
                AppRoute.Guide.route -> BottomTab.GUIDE
                AppRoute.Settings.route -> BottomTab.SETTINGS
                else -> BottomTab.SOS   // Radar 같은 상세 화면
            }

            Scaffold(
                bottomBar = {
                    BottomNavBar(
                        selected = currentTab,
                        onSelect = { tab ->

                            val targetRoute = when (tab) {
                                BottomTab.SOS -> AppRoute.SosInbox.route
                                BottomTab.GUIDE -> AppRoute.Guide.route
                                BottomTab.SETTINGS -> AppRoute.Settings.route
                            }

                            // 🔥 Radar 위에 있을 때 SOS 누르면 pop
                            if (currentRoute == AppRoute.Radar.route &&
                                targetRoute == AppRoute.SosInbox.route
                            ) {
                                navController.popBackStack()
                            } else {
                                navController.navigate(targetRoute) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            ) { padding ->

                NavHost(
                    navController = navController,
                    startDestination = AppRoute.SosInbox.route,
                    modifier = Modifier.padding(padding)
                ) {

                    composable(AppRoute.SosInbox.route) {
                        SosInboxRoute(
                            onOpenRadar = {
                                navController.navigate(AppRoute.Radar.route)
                            },
                            onNavigateToCompose = {
                                navController.navigate(AppRoute.SosCompose.route)
                            }
                        )
                    }

                    composable(AppRoute.Radar.route) {
                        RadarRoute(factory = factory)
                    }

                    composable(AppRoute.SosCompose.route) {
                        SosComposeRoute(
                            navController = navController,
                            reachControlUseCase = reachControl
                        )
                    }

                    composable(AppRoute.Guide.route) {
                        /* GuideRoute */
                    }

                    composable(AppRoute.Settings.route) {
                        /* SettingsRoute */
                    }

                }
            }
        }


    }
}
