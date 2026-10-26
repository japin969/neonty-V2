// Neont Messenger - BottomNav
// Bottom Navigation Bar Telegram: Contacts, Calls, Chats, dan Settings
package app.neon.com.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.neon.com.ui.theme.*
import app.neon.com.ui.theme.*
import app.neon.com.ui.theme.*

val MainTab.label: String get() = this.title

@Composable
fun NeontBottomNav(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    unreadMessagesCount: Int = 0,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(TelegramDarkHeader),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        MainTab.values().forEach { tab ->
            val isSelected = tab == selectedTab
            val activeColor = if (isSelected) TelegramLightBlue else Color(0xFF7F92A2)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onTabSelected(tab) },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(contentAlignment = Alignment.TopEnd) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label,
                            tint = activeColor,
                            modifier = Modifier.size(24.dp)
                        )

                        // Badge unread counter untuk tab Chats
                        if (tab == MainTab.CHATS && unreadMessagesCount > 0) {
                            Box(
                                modifier = Modifier
                                    .offset(x = 8.dp, y = (-4).dp)
                                    .clip(CircleShape)
                                    .background(BadgeUnread)
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "$unreadMessagesCount",
                                    color = Color.White,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = tab.label,
                        color = activeColor,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
