package app.neon.com.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.neon.com.data.model.AppSettings
import app.neon.com.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSettingsScreen(
    settings: AppSettings,
    onSaveSettings: (AppSettings) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentLanguage by remember { mutableStateOf(settings.language) }

    val languages = listOf(
        "Bahasa Indonesia" to "Bahasa Indonesia (Default)",
        "English" to "English (United States)",
        "Русский" to "Russian (Русский)",
        "Español" to "Spanish (Español)"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Language", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TelegramDarkHeader)
            )
        },
        containerColor = TelegramDarkBG,
        modifier = modifier
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            item {
                Text("PILIH BAHASA APLIKASI", color = TelegramPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp, 12.dp, 16.dp, 4.dp))
            }
            items(languages.size) { idx ->
                val (langKey, label) = languages[idx]
                val isSelected = currentLanguage == langKey
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            currentLanguage = langKey
                            onSaveSettings(settings.copy(language = langKey))
                            Toast.makeText(context, "Bahasa diubah ke $langKey", Toast.LENGTH_SHORT).show()
                        }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = isSelected, onClick = null)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(langKey, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Text(label, color = Color.LightGray, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
