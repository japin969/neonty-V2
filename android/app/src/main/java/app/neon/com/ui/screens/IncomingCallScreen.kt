// Neont Messenger - IncomingCallScreen.kt
// Pixel-Perfect Telegram Android Incoming Call Screen with Pulse Ring Animation
package app.neon.com.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.neon.com.ui.components.TelegramAvatar
import app.neon.com.ui.theme.*

@Composable
fun IncomingCallScreen(
    callerName: String,
    onAcceptCall: () -> Unit,
    onRejectCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Pulse ring animation pada avatar (membesar dan memudar berulang)
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_ring")
    val ringScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_scale"
    )
    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0F1721),
                        Color(0xFF17212B),
                        Color(0xFF0B1015)
                    )
                )
            )
            .padding(24.dp)
    ) {
        // Caller Info with Pulsing Rings
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(190.dp)
            ) {
                // Outer Pulse Ring
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(ringScale)
                        .alpha(ringAlpha)
                        .clip(CircleShape)
                        .background(TelegramPrimary)
                )

                // Avatar 120dp
                TelegramAvatar(
                    name = callerName,
                    size = 120
                )
            }

            Spacer(Modifier.height(24.dp))

            // Nama: 24sp putih
            Text(
                text = callerName,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            // Status: "Incoming call..." 16sp abu-abu
            Text(
                text = "Incoming audio call...",
                color = TelegramTextSecondary, // #8B9DAE
                fontSize = 16.sp
            )
        }

        // Bottom Controls: Tolak (Merah #E53935) & Terima (Hijau #4CAF50)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp, start = 32.dp, end = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Decline Button (Red)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(TelegramRed) // #E53935
                        .clickable { onRejectCall() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "Decline",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Decline",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }

            // Accept Button (Green)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(TelegramGreen) // #4CAF50
                        .clickable { onAcceptCall() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Accept",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Accept",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }
    }
}
