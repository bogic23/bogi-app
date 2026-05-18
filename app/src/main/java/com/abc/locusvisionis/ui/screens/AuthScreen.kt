package com.abc.locusvisionis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.abc.locusvisionis.ui.theme.DashboardTheme

enum class AuthMode {
    Login,
    Register
}

@Composable
fun AuthScreen(
    authMode: AuthMode,
    fullName: String,
    email: String,
    password: String,
    confirmPassword: String,
    isLoading: Boolean,
    errorMessage: String?,
    onModeChange: (AuthMode) -> Unit,
    onFullNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    val appColors = DashboardTheme.colors

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(appColors.gradientStart, appColors.background)
                )
            )
            .padding(24.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = appColors.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Personal Dashboard",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = appColors.textPrimary
                )

                Text(
                    text = if (authMode == AuthMode.Login) {
                        "Sign in with Firebase Authentication to open your dashboard and load your Firestore profile."
                    } else {
                        "Create a new Firebase account and we’ll save your basic profile to Firestore."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = appColors.textSecondary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AuthModeButton(
                        label = "Login",
                        selected = authMode == AuthMode.Login,
                        onClick = { onModeChange(AuthMode.Login) },
                        modifier = Modifier.weight(1f)
                    )
                    AuthModeButton(
                        label = "Register",
                        selected = authMode == AuthMode.Register,
                        onClick = { onModeChange(AuthMode.Register) },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (authMode == AuthMode.Register) {
                    AuthInputField(
                        value = fullName,
                        onValueChange = onFullNameChange,
                        label = "Full Name"
                    )
                }

                AuthInputField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = "Email"
                )

                AuthInputField(
                    value = password,
                    onValueChange = onPasswordChange,
                    label = "Password",
                    isPassword = true
                )

                if (authMode == AuthMode.Register) {
                    AuthInputField(
                        value = confirmPassword,
                        onValueChange = onConfirmPasswordChange,
                        label = "Confirm Password",
                        isPassword = true
                    )
                }

                if (!errorMessage.isNullOrBlank()) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = appColors.accentRed
                    )
                }

                Button(
                    onClick = onSubmit,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = appColors.primary)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .height(18.dp)
                                .width(18.dp),
                            strokeWidth = 2.dp,
                            color = appColors.surface
                        )
                    } else {
                        Text(if (authMode == AuthMode.Login) "Sign In" else "Create Account")
                    }
                }

                Text(
                    text = if (authMode == AuthMode.Login) {
                        "Your profile page will reflect the Firebase user and Firestore document after login."
                    } else {
                        "Registration creates a Firestore document in users/{uid} with your name, email, and timestamps."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = appColors.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                TextButton(
                    onClick = {
                        onModeChange(
                            if (authMode == AuthMode.Login) AuthMode.Register else AuthMode.Login
                        )
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = if (authMode == AuthMode.Login) {
                            "Need an account? Register"
                        } else {
                            "Already registered? Login"
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AuthModeButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appColors = DashboardTheme.colors

    if (selected) {
        Button(
            onClick = onClick,
            modifier = modifier,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = appColors.primary)
        ) {
            Text(label)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(label, color = appColors.textPrimary)
        }
    }
}

@Composable
private fun AuthInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPassword: Boolean = false
) {
    val appColors = DashboardTheme.colors

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = appColors.cardBackground,
            unfocusedContainerColor = appColors.cardBackground,
            focusedIndicatorColor = appColors.primary,
            unfocusedIndicatorColor = appColors.textLight.copy(alpha = 0.45f),
            focusedLabelColor = appColors.primary,
            unfocusedLabelColor = appColors.textSecondary
        )
    )
}
