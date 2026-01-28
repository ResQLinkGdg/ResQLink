package com.example.resqlink.ui.feature_sos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class SosInboxActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SosInboxRoute()
        }
    }
}
