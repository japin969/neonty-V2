package app.neon.com.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.neon.com.ui.theme.TelegramSurface

data class AttachOption(
    val title: String,
    val icon: ImageVector,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachBottomSheet(
    onDismiss: () -> Unit,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = listOf(
        AttachOption("Gallery", Icons.Default.Image, Color(0xFF3B82F6)),
        AttachOption("File", Icons.Default.InsertDriveFile, Color(0xFFF59E0B)),
        AttachOption("Location", Icons.Default.LocationOn, Color(0xFF10B981)),
        AttachOption("Poll", Icons.Default.Poll, Color(0xFF8B5CF6)),
        AttachOption("Music", Icons.Default.MusicNote, Color(0xFFEC4899)),
        AttachOption("Contact", Icons.Default.Person, Color(0xFF06B6D4))
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = TelegramSurface,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                options.take(3).forEach { option ->
                    AttachItem(option = option, onClick = { onOptionSelected(option.title) })
                }
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                options.drop(3).forEach { option ->
                    AttachItem(option = option, onClick = { onOptionSelected(option.title) })
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AttachItem(
    option: AttachOption,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(option.color),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = option.icon,
                contentDescription = option.title,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = option.title,
            color = Color.White,
            fontSize = 12.sp
        )
    }
}
