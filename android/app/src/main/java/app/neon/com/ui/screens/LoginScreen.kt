package app.neon.com.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.neon.com.ui.theme.TelegramDarkBG
import app.neon.com.ui.theme.TelegramPrimary
import app.neon.com.ui.theme.TelegramSurface
import app.neon.com.ui.theme.TelegramTextPrimary
import app.neon.com.ui.theme.TelegramTextSecondary

data class CountryCode(val name: String, val dialCode: String, val flag: String)

val DEFAULT_COUNTRIES = listOf(
    CountryCode("Indonesia", "+62", "🇮🇩"),
    CountryCode("United States", "+1", "🇺🇸"),
    CountryCode("United Kingdom", "+44", "🇬🇧"),
    CountryCode("Germany", "+49", "🇩🇪"),
    CountryCode("Singapore", "+65", "🇸🇬"),
    CountryCode("Japan", "+81", "🇯🇵"),
    CountryCode("United Arab Emirates", "+971", "🇦🇪")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onPhoneSubmit: (phoneNumber: String) -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    modifier: Modifier = Modifier
) {
    var selectedCountry by remember { mutableStateOf(DEFAULT_COUNTRIES[0]) }
    var phoneNumber by remember { mutableStateOf("") }
    var isCountryPickerOpen by remember { mutableStateOf(false) }
    var syncContacts by remember { mutableStateOf(true) }
    val focusManager = LocalFocusManager.current

    val canProceed = phoneNumber.trim().length >= 7 && !isLoading

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Your Phone",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TelegramTextPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TelegramDarkBG
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (canProceed) {
                        focusManager.clearFocus()
                        val fullNumber = "${selectedCountry.dialCode}${phoneNumber.trim()}"
                        onPhoneSubmit(fullNumber)
                    }
                },
                containerColor = if (canProceed) TelegramPrimary else Color(0xFF2C3847),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Next"
                    )
                }
            }
        },
        containerColor = TelegramDarkBG
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Please confirm your country code and enter your phone number.",
                color = TelegramTextSecondary,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            Spacer(Modifier.height(28.dp))

            // Country Selector Row
            Box {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(TelegramSurface)
                        .clickable { isCountryPickerOpen = true }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(selectedCountry.flag, fontSize = 20.sp)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = selectedCountry.name,
                            color = TelegramTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Select Country",
                        tint = TelegramTextSecondary
                    )
                }

                DropdownMenu(
                    expanded = isCountryPickerOpen,
                    onDismissRequest = { isCountryPickerOpen = false },
                    modifier = Modifier
                        .background(TelegramSurface)
                        .fillMaxWidth(0.85f)
                ) {
                    DEFAULT_COUNTRIES.forEach { country ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("${country.flag}  ${country.name}", color = TelegramTextPrimary)
                                    Text(country.dialCode, color = TelegramPrimary, fontWeight = FontWeight.Bold)
                                }
                            },
                            onClick = {
                                selectedCountry = country
                                isCountryPickerOpen = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Phone Input Row with Dial Code
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Country Code
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(TelegramSurface)
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = selectedCountry.dialCode,
                        color = TelegramPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.width(12.dp))

                // Number Input
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }
                        if (digits.length <= 15) {
                            phoneNumber = digits
                        }
                    },
                    placeholder = {
                        Text("812 3456 7890", color = Color(0xFF637381))
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (canProceed) {
                                focusManager.clearFocus()
                                val fullNumber = "${selectedCountry.dialCode}${phoneNumber.trim()}"
                                onPhoneSubmit(fullNumber)
                            }
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = TelegramSurface,
                        unfocusedContainerColor = TelegramSurface,
                        focusedTextColor = TelegramTextPrimary,
                        unfocusedTextColor = TelegramTextPrimary,
                        focusedBorderColor = TelegramPrimary,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                )
            }

            // Sync Contacts Row
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { syncContacts = !syncContacts }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = syncContacts,
                    onCheckedChange = { syncContacts = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = TelegramPrimary,
                        checkmarkColor = Color.White
                    )
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Sync Contacts",
                    color = TelegramTextPrimary,
                    fontSize = 14.sp
                )
            }

            AnimatedVisibility(visible = errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = Color(0xFFFF5252),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Spacer(Modifier.weight(1f))

            // Quick Demo Login hint
            TextButton(
                onClick = {
                    onPhoneSubmit("+6281234567890")
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = "Quick Demo Sign In (Pavel Durov)",
                    color = TelegramPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
