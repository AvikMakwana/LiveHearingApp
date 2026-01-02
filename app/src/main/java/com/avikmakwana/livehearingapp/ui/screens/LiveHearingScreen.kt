package com.avikmakwana.livehearingapp.ui.screens


import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.avikmakwana.livehearingapp.ui.HearingViewModel

@Composable
fun LiveHearingScreen(
    viewModel: HearingViewModel = hiltViewModel()
) {
    val isListening by viewModel.isListening.collectAsState()
    val amplitude by viewModel.amplitude.collectAsState()
    val context = LocalContext.current

    // Permission Handling
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        hasPermission = perms[Manifest.permission.RECORD_AUDIO] == true
    }

    Scaffold(
        containerColor = Color(0xFF121212) // Deep Dark Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Live Hearing",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isListening) "Active Environment" else "Paused",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isListening) Color(0xFF00E676) else Color.Gray
                )
            }

            // Visualizer Centerpiece
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(300.dp)
            ) {
                if (isListening) {
                    // Outer Ripple
                    PulsingCircle(
                        scale = 1f + (amplitude / 100f) * 0.5f,
                        color = Color(0xFF2979FF).copy(alpha = 0.2f)
                    )
                    // Inner Ripple
                    PulsingCircle(
                        scale = 1f + (amplitude / 100f) * 0.3f,
                        color = Color(0xFF2979FF).copy(alpha = 0.4f)
                    )
                }

                // Static Icon Container
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF2979FF), Color(0xFF2962FF))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Hearing,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            // Controls
            Button(
                onClick = {
                    if (!hasPermission) {
                        val perms = mutableListOf(Manifest.permission.RECORD_AUDIO)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            perms.add(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        permissionLauncher.launch(perms.toTypedArray())
                    } else {
                        viewModel.toggleListening()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isListening) Color(0xFFCF6679) else Color(0xFF2979FF)
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isListening) "Stop Listening" else "Start Listening",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun PulsingCircle(scale: Float, color: Color) {
    // Smooth animation for amplitude changes
    val animatedScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = tween(durationMillis = 100, easing = LinearEasing),
        label = "scale"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            color = color,
            radius = (size.minDimension / 2) * animatedScale,
            center = center
        )
    }
}