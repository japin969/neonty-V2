// Neont Messenger - TelegramAvatar.kt
// Avatar Khas Telegram Android: Gradien Warna Otomatis berdasarkan Inisial & Online Dot Indicator
package app.neon.com.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import app.neon.com.ui.theme.*
import app.neon.com.ui.theme.*
import kotlin.math.abs

@Composable
fun TelegramAvatar(
    name: String,
    avatarUrl: String? = null,
    size: Int = 48,
    isOnline: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val initials = remember(name) {
        val parts = name.trim().split(" ")
        if (parts.size >= 2) {
            "${parts[0].take(1)}${parts[1].take(1)}".uppercase()
        } else {
            name.take(2).uppercase()
        }
    }

    val gradientColors = remember(name) {
        val index = abs(name.hashCode()) % AvatarGradients.size
        AvatarGradients[index]
    }

    val imageRequest = remember(avatarUrl) {
        if (!avatarUrl.isNullOrBlank()) {
            ImageRequest.Builder(context)
                .data(avatarUrl)
                .crossfade(true)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .build()
        } else null
    }

    Box(
        modifier = modifier.size(size.dp),
        contentAlignment = Alignment.Center
    ) {
        if (imageRequest != null) {
            AsyncImage(
                model = imageRequest,
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Brush.linearGradient(gradientColors)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = (size * 0.4).sp
                )
            }
        }

        // Online indicator dot di sudut kanan bawah
        if (isOnline) {
            Box(
                modifier = Modifier
                    .size((size * 0.28).dp)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .border(2.dp, Color(0xFF17212B), CircleShape)
                    .background(OnlineGreen)
            )
        }
    }
}
