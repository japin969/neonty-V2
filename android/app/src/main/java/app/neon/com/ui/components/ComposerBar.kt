package app.neon.com.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.neon.com.ui.theme.*

@Composable
fun ComposerBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    onTyping: (Boolean) -> Unit = {},
    onAttachClick: () -> Unit = {},
    onEmojiClick: () -> Unit = {},
    onMicClick: () -> Unit = {},
    replyingTo: String? = null,
    onCancelReply: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    LaunchedEffect(text) {
        onTyping(text.isNotBlank())
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp)
            .background(TelegramSurface)
    ) {
        // UI Membalas
        if (replyingTo != null) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.width(3.dp).height(30.dp).background(TelegramPrimary))
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Membalas", color = TelegramPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(replyingTo, color = Color.White, fontSize = 14.sp, maxLines = 1)
                }
                IconButton(onClick = onCancelReply, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, "Cancel", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(24.dp)).background(TelegramInputBG).padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onEmojiClick, modifier = Modifier.size(38.dp)) {
                    Icon(Icons.Default.Mood, "Emoji", tint = TelegramTextSecondary, modifier = Modifier.size(24.dp))
                }

                Box(modifier = Modifier.weight(1f).wrapContentHeight().padding(horizontal = 6.dp, vertical = 8.dp), contentAlignment = Alignment.CenterStart) {
                    if (text.isEmpty()) Text("Message", color = TelegramTextSecondary, fontSize = 16.sp)
                    BasicTextField(
                        value = text, onValueChange = onTextChange,
                        textStyle = TextStyle(color = Color.White, fontSize = 16.sp, lineHeight = 20.sp),
                        cursorBrush = SolidColor(TelegramPrimary), minLines = 1, maxLines = 5,
                        modifier = Modifier.fillMaxWidth().wrapContentHeight()
                    )
                }

                IconButton(onClick = onAttachClick, modifier = Modifier.size(38.dp)) {
                    Icon(Icons.Default.AttachFile, "Attach", tint = TelegramTextSecondary, modifier = Modifier.size(22.dp).rotate(-45f))
                }
            }

            Spacer(Modifier.width(8.dp))

            val hasText = text.isNotBlank()
            Box(
                modifier = Modifier.size(46.dp).clip(CircleShape)
                    .background(if (hasText) TelegramPrimary else Color.Transparent)
                    .clickable {
                        if (hasText) {
                            val msg = text.trim()
                            onSendMessage(msg)
                        } else onMicClick()
                    },
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = hasText,
                    transitionSpec = {
                        (fadeIn(tween(200)) + scaleIn(initialScale = 0.2f))
                            .togetherWith(fadeOut(tween(180)) + scaleOut(targetScale = 0.2f))
                    }, label = "composer_btn"
                ) { targetHasText ->
                    val rotation by animateFloatAsState(if (targetHasText) 0f else -90f, tween(200), label = "rot")
                    if (targetHasText) Icon(Icons.Default.Send, "Send", tint = Color.White, modifier = Modifier.size(22.dp).rotate(-15f + rotation))
                    else Icon(Icons.Default.Mic, "Record", tint = TelegramTextSecondary, modifier = Modifier.size(24.dp).rotate(rotation + 90f))
                }
            }
        }
    }
}
