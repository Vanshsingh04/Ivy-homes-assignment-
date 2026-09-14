package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import data.ApiClient
import data.TokenStorage
import kotlinx.coroutines.launch
import theme.IvyColors


@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var email by remember { mutableStateOf("demo1@ivy.homes") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Check if already logged in
    LaunchedEffect(Unit) {
        if (TokenStorage.hasValidSession()) {
            if (!TokenStorage.isTokenExpired() || ApiClient.refreshToken()) {
                onLoginSuccess()
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(IvyColors.Background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo
            Text(
                text = "IVY HOMES",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = IvyColors.Primary,
                letterSpacing = 4.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Premium Property Search",
                fontSize = 14.sp,
                color = IvyColors.TextSecondary,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Login card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = IvyColors.Surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Sign In",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = IvyColors.TextPrimary
                    )

                    // Email
                    Column {
                        Text("Email", fontSize = 12.sp, color = IvyColors.TextSecondary, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it; error = null },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = IvyColors.SurfaceVariant,
                                focusedContainerColor = IvyColors.SurfaceVariant,
                                unfocusedBorderColor = IvyColors.Border,
                                focusedBorderColor = IvyColors.Primary,
                                cursorColor = IvyColors.Primary
                            ),
                            textStyle = LocalTextStyle.current.copy(color = IvyColors.TextPrimary)
                        )
                    }

                    // Password
                    Column {
                        Text("Password", fontSize = 12.sp, color = IvyColors.TextSecondary, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it; error = null },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = IvyColors.SurfaceVariant,
                                focusedContainerColor = IvyColors.SurfaceVariant,
                                unfocusedBorderColor = IvyColors.Border,
                                focusedBorderColor = IvyColors.Primary,
                                cursorColor = IvyColors.Primary
                            ),
                            textStyle = LocalTextStyle.current.copy(color = IvyColors.TextPrimary)
                        )
                    }

                    // Error
                    error?.let {
                        Text(
                            text = it,
                            fontSize = 13.sp,
                            color = IvyColors.Error
                        )
                    }

                    // Login button
                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                error = null
                                val result = ApiClient.login(email.trim(), password.trim())
                                isLoading = false
                                result.fold(
                                    onSuccess = { onLoginSuccess() },
                                    onFailure = { error = "Login failed: ${it.message}" }
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = IvyColors.Primary,
                            contentColor = IvyColors.OnPrimary,
                            disabledContainerColor = IvyColors.Primary.copy(alpha = 0.4f)
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = IvyColors.OnPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Sign In", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Demo accounts: demo1@ivy.homes, demo2@ivy.homes, demo3@ivy.homes",
                fontSize = 11.sp,
                color = IvyColors.TextMuted
            )
        }
    }
}
