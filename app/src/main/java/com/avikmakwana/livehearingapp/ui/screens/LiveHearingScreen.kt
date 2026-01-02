package com.avikmakwana.livehearingapp.ui.screens

import android.Manifest
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
import androidx.compose.material.icons.rounded.Hearing
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

@Composable
fun LiveHearingScreen(
    viewModel: HearingViewModel = hiltViewModel()
) {
    val isListening by viewModel.isListening.collectAsState()
    val amplitude by viewModel.amplitude.collectAsState()
    val context = LocalContext.current

    // Error Handling from ViewModel/Engine (Requires exposing error flow in VM,
    // for now we handle via simple Toast logic if Engine throws)

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WeHearBlack)
    ) {
        // --- 1. Top Bar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 24.dp, end = 24.dp),
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
                Icon(Icons.Rounded.Settings, contentDescription = "Settings", tint = Color.White)
            }
        }

        // --- 2. Central Visualization ---
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // The "Soul" of the UI: The Dynamic Visualizer
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(320.dp)
            ) {
                if (isListening) {
                    // Layered Ripples
                    WeHearRipple(amplitude = amplitude, delay = 0)
                    WeHearRipple(amplitude = amplitude, delay = 300)
                    WeHearRipple(amplitude = amplitude, delay = 600)
                }

                // The Main Button Container
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(
                            brush = if (isListening) WeHearGradient else SolidColor(WeHearDarkGrey)
                        )
                        .clickable {
                            val perms = mutableListOf(Manifest.permission.RECORD_AUDIO)
                            if (android.os.Build.VERSION.SDK_INT >= 33) perms.add(Manifest.permission.POST_NOTIFICATIONS)
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

            Spacer(modifier = Modifier.height(40.dp))

            // Status Text with Animation
            AnimatedContent(targetState = isListening, label = "Status") { listening ->
                if (listening) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "ACTIVE LISTENING",
                            color = WeHearBlue,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "Analyzing Environment...",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    Text(
                        text = "TAP TO ACTIVATE",
                        color = Color.DarkGray,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }
            }
        }

        // --- 3. Bottom Control Deck ---
        // Mimicking "Pro" audio controls
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp, start = 24.dp, end = 24.dp)
                .fillMaxWidth()
                .height(80.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(WeHearDarkGrey.copy(alpha = 0.8f)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ControlItem(icon = Icons.Rounded.Tune, label = "EQ")
            VerticalDivider(
                modifier = Modifier.height(30.dp),
                color = Color.Gray.copy(alpha = 0.3f)
            )
            ControlItem(icon = Icons.Rounded.Hearing, label = "Focus", isActive = true)
            VerticalDivider(
                modifier = Modifier.height(30.dp),
                color = Color.Gray.copy(alpha = 0.3f)
            )
            ControlItem(icon = Icons.Rounded.Settings, label = "Mode")
        }
    }
}

@Composable
fun ControlItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean = false
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isActive) WeHearBlue else Color.Gray
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, color = if (isActive) Color.White else Color.Gray, fontSize = 10.sp)
    }
}

@Composable
fun WeHearRipple(amplitude: Int, delay: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "ripple")

    // Animate scale based on amplitude + infinite pulse
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, delayMillis = delay, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ), label = "pulse"
    )

    // Dynamic scale reacts to real audio amplitude
    val audioScale = 1f + (amplitude / 200f)

    Canvas(modifier = Modifier.fillMaxSize()) {
        val radius = (size.minDimension / 2) * 0.5f // Base radius matches button

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(WeHearBlue.copy(alpha = 0.3f), Color.Transparent),
                center = center,
                radius = radius * pulse * audioScale * 1.5f
            ),
            radius = radius * pulse * audioScale,
            center = center
        )

        // Add a thin stroke for a "Tech" look
        drawCircle(
            color = WeHearBlue.copy(alpha = 0.1f * (1.5f - pulse)), // Fade out as it expands
            radius = radius * pulse * audioScale,
            center = center,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}