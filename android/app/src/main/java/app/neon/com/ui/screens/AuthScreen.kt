// Neont Messenger - AuthScreen.kt
// Pixel-Perfect Telegram Android Email & Password Authentication Screen
package app.neon.com.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.neon.com.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onLogin: (email: String, pass: String) -> Unit,
    onRegister: (email: String, pass: String) -> Unit,
    errorMessage: String? = null,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    var isRegisterMode by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val canProceed = email.isNotBlank() && password.length >= 6 && !isLoading

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // Header teks besar "Your Email" (warna biru #2481CC, 20sp, Bold)
                    Text(
                        text = if (isRegisterMode) "Create Account" else "Your Email",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TelegramPrimary // #2481CC
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TelegramDarkBG
                )
            )
        },
        containerColor = TelegramDarkBG,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = if (isRegisterMode) {
                    "Please enter your email and choose a secure password to register."
                } else {
                    "Please enter your email address and password to sign in."
                },
                color = TelegramTextSecondary, // #8B9DAE
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            Spacer(Modifier.height(32.dp))

            // Input Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email", color = TelegramTextSecondary) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = TelegramSurface,
                    unfocusedContainerColor = TelegramSurface,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = TelegramPrimary, // Garis bawah/border warna biru jika sedang diketik
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = TelegramPrimary
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // Input Password
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", color = TelegramTextSecondary) },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (canProceed) {
                            focusManager.clearFocus()
                            if (isRegisterMode) onRegister(email, password) else onLogin(email, password)
                        }
                    }
                ),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = TelegramTextSecondary
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = TelegramSurface,
                    unfocusedContainerColor = TelegramSurface,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = TelegramPrimary, // Garis bawah/border warna biru jika sedang diketik
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = TelegramPrimary
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            )

            AnimatedVisibility(visible = errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = TelegramRed,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Spacer(Modifier.weight(1f))

            // Tombol "NEXT" / "LOGIN" berwarna biru (#2481CC) full width, tinggi 56dp
            Button(
                onClick = {
                    focusManager.clearFocus()
                    if (isRegisterMode) {
                        onRegister(email.trim(), password.trim())
                    } else {
                        onLogin(email.trim(), password.trim())
                    }
                },
                enabled = canProceed,
                colors = ButtonDefaults.buttonColors(
                    containerColor = TelegramPrimary, // #2481CC
                    disabledContainerColor = TelegramSurface
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (isRegisterMode) "SIGN UP" else "NEXT",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Teks kecil di bawah: Toggle antara mode Login dan Register
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isRegisterMode) "Already have an account? " else "Don't have an account? ",
                    color = TelegramTextSecondary,
                    fontSize = 13.sp
                )
                Text(
                    text = if (isRegisterMode) "Sign In" else "Sign Up",
                    color = TelegramPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        isRegisterMode = !isRegisterMode
                    }
                )
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}
