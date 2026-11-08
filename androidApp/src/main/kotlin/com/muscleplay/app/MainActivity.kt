package com.muscleplay.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.muscleplay.app.core.network.AndroidBaseUrlConfig
import com.muscleplay.app.core.network.auth.AndroidAppContext
import com.muscleplay.app.navigation.NotificationIntentHandler

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidAppContext.applicationContext = applicationContext
        AndroidBaseUrlConfig.baseUrl = BuildConfig.BASE_URL
        NotificationIntentHandler.handleIntent(intent)
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            App()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        NotificationIntentHandler.handleIntent(intent)
    }
}
