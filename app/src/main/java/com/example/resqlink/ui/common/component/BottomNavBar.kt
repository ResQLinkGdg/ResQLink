package com.example.resqlink.ui.common.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.resqlink.ui.common.model.BottomTab

@Composable
fun BottomNavBar(
    selected: BottomTab,
    onSelect: (BottomTab) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = selected == BottomTab.SOS,
            onClick = { onSelect(BottomTab.SOS) },
            icon = { Icon(Icons.Outlined.MyLocation, contentDescription = null) },
            label = { Text("수신함") }
        )
        NavigationBarItem(
            selected = selected == BottomTab.GUIDE,
            onClick = { onSelect(BottomTab.GUIDE) },
            icon = { Icon(Icons.Outlined.Book, contentDescription = null) },
            label = { Text("응급가이드") }
        )
        NavigationBarItem(
            selected = selected == BottomTab.SETTINGS,
            onClick = { onSelect(BottomTab.SETTINGS) },
            icon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
            label = { Text("설정") }
        )
    }
}
