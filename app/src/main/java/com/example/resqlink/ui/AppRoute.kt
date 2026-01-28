package com.example.resqlink.ui

sealed class AppRoute(val route: String) {
    object SosInbox : AppRoute("sos_inbox")
    object SosCompose : AppRoute("sos_compose")
    object Radar : AppRoute("radar")        // 하단바에 직접 안 걸릴 수도 있음
    object Guide : AppRoute("guide")
    object Settings : AppRoute("settings")
}
