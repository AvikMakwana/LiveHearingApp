package com.avikmakwana.livehearingapp.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Hearing
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.avikmakwana.livehearingapp.ui.HearingViewModel

// BRAND COLORS
val WeHearBlack = Color(0xFF0F1115)
val WeHearDarkGrey = Color(0xFF1E2129)
val WeHearBlue = Color(0xFF00E5FF)
val WeHearPurple = Color(0xFF651FFF)
val WeHearGradient = Brush.linearGradient(
    colors = listOf(WeHearBlue, WeHearPurple),
    start = Offset(0f, 0f),
    end = Offset(500f, 500f)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveHearingScreen(
    viewModel: HearingViewModel = hiltViewModel()
) {
    val isListening by viewModel.isListening.collectAsState()
    val amplitude by viewModel.amplitude.collectAsState()
    val balance by viewModel.balance.collectAsState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    Scaffold(
        containerColor = WeHearBlack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // --- 1. Top Bar ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, start = 24.dp, end = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "WeHear",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Smart Hearing Ecosystem",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        letterSpacing = 2.sp
                    )
                }
                IconButton(
                    onClick = { /* TODO: Settings */ },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(WeHearDarkGrey)
                ) {
                    Icon(
                        Icons.Rounded.Settings,
                        contentDescription = "Settings",
                        tint = Color.White
                    )
                }
            }

            // --- 2. Central Visualization ---
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // Background Pulse
                if (isListening) {
                    WeHearRipple(amplitude = amplitude, delay = 0)
                    WeHearRipple(amplitude = amplitude, delay = 300)
                }

                // Main Toggle Button
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(if (isListening) WeHearGradient else SolidColor(WeHearDarkGrey))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            val perms = mutableListOf(Manifest.permission.RECORD_AUDIO)
                            if (Build.VERSION.SDK_INT >= 33) perms.add(Manifest.permission.POST_NOTIFICATIONS)
                            permissionLauncher.launch(perms.toTypedArray())
                            viewModel.toggleListening()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Rounded.Hearing else Icons.Rounded.PowerSettingsNew,
                        contentDescription = "Toggle",
                        tint = Color.White,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }

            // Status Text
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedContent(targetState = isListening, label = "Status") { listening ->
                    Text(
                        text = if (listening) "ACTIVE LISTENING" else "TAP TO ACTIVATE",
                        color = if (listening) WeHearBlue else Color.DarkGray,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- 3. Balance Control (Replacing EQ) ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .background(WeHearDarkGrey, RoundedCornerShape(24.dp))
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "AUDIO BALANCE",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        Icons.Rounded.GraphicEq,
                        contentDescription = null,
                        tint = WeHearBlue,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "L",
                        color = if (balance < 0) Color.White else Color.Gray,
                        fontWeight = FontWeight.Bold
                    )

                    Slider(
                        value = balance,
                        onValueChange = { viewModel.updateBalance(it) },
                        valueRange = -1f..1f,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = WeHearBlue,
                            inactiveTrackColor = Color.Black
                        )
                    )

                    Text(
                        "R",
                        color = if (balance > 0) Color.White else Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Helper Text
                Text(
                    text = when {
                        balance < -0.2f -> "Focus Left"
                        balance > 0.2f -> "Focus Right"
                        else -> "Balanced"
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = WeHearBlue,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun WeHearRipple(amplitude: Int, delay: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "ripple")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1.0f, targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, delayMillis = delay, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ), label = "pulse"
    )
    val audioScale = 1f + (amplitude / 200f)

    Canvas(modifier = Modifier.fillMaxSize()) {
        val radius = (size.minDimension / 2) * 0.5f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(WeHearBlue.copy(alpha = 0.3f), Color.Transparent),
                center = center, radius = radius * pulse * audioScale * 1.5f
            ),
            radius = radius * pulse * audioScale,
            center = center
        )
        drawCircle(
            color = WeHearBlue.copy(alpha = 0.1f * (1.5f - pulse)),
            radius = radius * pulse * audioScale,
            center = center,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}