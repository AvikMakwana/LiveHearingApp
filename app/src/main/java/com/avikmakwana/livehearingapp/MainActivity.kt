package com.avikmakwana.livehearingapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.avikmakwana.livehearingapp.ui.screens.LiveHearingScreen
import com.avikmakwana.livehearingapp.ui.theme.LiveHearingAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LiveHearingAppTheme {
                LiveHearingScreen()
            }
        }
    }
}