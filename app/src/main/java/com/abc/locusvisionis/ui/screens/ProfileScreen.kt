package com.abc.locusvisionis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.abc.locusvisionis.FirebaseProfileState
import com.abc.locusvisionis.UserProfile
import com.abc.locusvisionis.toProfileState
import com.abc.locusvisionis.ui.theme.DashboardTheme
import com.abc.locusvisionis.ui.theme.ThemeOption
import com.google.firebase.auth.FirebaseUser

data class ProfileMenuEntry(
    val icon: ImageVector,
    val title: String,
    val subtitle: String = ""
)

@Composable
fun ProfileScreen(
    user: FirebaseUser?,
    profile: UserProfile?,
    selectedTheme: ThemeOption,
    onThemeChange: (ThemeOption) -> Unit,
    profileSaving: Boolean,
    verificationSending: Boolean,
    passwordUpdating: Boolean,
    onSaveProfile: (String, String) -> Unit,
    onSendVerificationEmail: () -> Unit,
    onRefreshVerification: () -> Unit,
    onChangePassword: (String, String) -> Unit,
    onSignOut: () -> Unit
) {
    val menuItems = profileMenuItems()
    val appColors = DashboardTheme.colors
    val profileState = user.toProfileState(profile)
    var editableName by remember(profileState.displayName) { mutableStateOf(profileState.displayName) }
    var editableBirthday by remember(profileState.birthday) { mutableStateOf(profileState.birthday) }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

    LaunchedEffect(profileState.displayName, profileState.birthday) {
        editableName = profileState.displayName
        editableBirthday = profileState.birthday
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = appColors.primary),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = profileState.initials,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = profileState.displayName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = profileState.secondaryText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.82f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = profileState.providerLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.92f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    profileState.stats.forEach { (value, label) ->
                        StatBox(value = value, label = label)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        FirestoreProfileCard(profileState = profileState)

        Spacer(modifier = Modifier.height(16.dp))

        AccountManagementCard(
            fullName = editableName,
            birthday = editableBirthday,
            email = profileState.secondaryText,
            isEmailVerified = profileState.isEmailVerified,
            profileSaving = profileSaving,
            verificationSending = verificationSending,
            passwordUpdating = passwordUpdating,
            currentPassword = currentPassword,
            newPassword = newPassword,
            onFullNameChange = { editableName = it },
            onBirthdayChange = { editableBirthday = it },
            onCurrentPasswordChange = { currentPassword = it },
            onNewPasswordChange = { newPassword = it },
            onSaveProfile = { onSaveProfile(editableName, editableBirthday) },
            onSendVerificationEmail = onSendVerificationEmail,
            onRefreshVerification = onRefreshVerification,
            onChangePassword = {
                onChangePassword(currentPassword, newPassword)
                currentPassword = ""
                newPassword = ""
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Settings",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = appColors.textPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        ThemeSelectorCard(
            selectedTheme = selectedTheme,
            onThemeChange = onThemeChange
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = appColors.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column {
                menuItems.forEachIndexed { index, item ->
                    ProfileMenuItem(item)
                    if (index < menuItems.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = appColors.textLight.copy(alpha = 0.1f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onSignOut,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = appColors.accentRed),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sign Out")
        }
    }
}

@Composable
private fun FirestoreProfileCard(profileState: FirebaseProfileState) {
    val appColors = DashboardTheme.colors

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = appColors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Firebase Profile",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = appColors.textPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            profileState.details.forEachIndexed { index, (label, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = appColors.textSecondary
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium,
                        color = appColors.textPrimary,
                        textAlign = TextAlign.End,
                        modifier = Modifier.width(180.dp)
                    )
                }

                if (index < profileState.details.lastIndex) {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun AccountManagementCard(
    fullName: String,
    birthday: String,
    email: String,
    isEmailVerified: Boolean,
    profileSaving: Boolean,
    verificationSending: Boolean,
    passwordUpdating: Boolean,
    currentPassword: String,
    newPassword: String,
    onFullNameChange: (String) -> Unit,
    onBirthdayChange: (String) -> Unit,
    onCurrentPasswordChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onSaveProfile: () -> Unit,
    onSendVerificationEmail: () -> Unit,
    onRefreshVerification: () -> Unit,
    onChangePassword: () -> Unit
) {
    val appColors = DashboardTheme.colors

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = appColors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Manage Account",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = appColors.textPrimary
            )

            ProfileInputField(
                value = fullName,
                onValueChange = onFullNameChange,
                label = "Full Name"
            )

            ProfileInputField(
                value = birthday,
                onValueChange = onBirthdayChange,
                label = "Birthday",
                placeholder = "YYYY-MM-DD"
            )

            Button(
                onClick = onSaveProfile,
                enabled = !profileSaving,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = appColors.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (profileSaving) "Saving..." else "Save Profile")
            }

            HorizontalDivider(color = appColors.textLight.copy(alpha = 0.15f))

            Text(
                text = "Email verification",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = appColors.textPrimary
            )

            Text(
                text = if (isEmailVerified) {
                    "$email is verified."
                } else {
                    "$email is not verified yet."
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (isEmailVerified) appColors.accentGreen else appColors.accentOrange
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onSendVerificationEmail,
                    enabled = !isEmailVerified && !verificationSending,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = appColors.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (verificationSending) "Sending..." else "Send Email")
                }

                TextButton(
                    onClick = onRefreshVerification,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Refresh Status")
                }
            }

            HorizontalDivider(color = appColors.textLight.copy(alpha = 0.15f))

            Text(
                text = "Change password",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = appColors.textPrimary
            )

            ProfileInputField(
                value = currentPassword,
                onValueChange = onCurrentPasswordChange,
                label = "Current Password",
                isPassword = true
            )

            ProfileInputField(
                value = newPassword,
                onValueChange = onNewPasswordChange,
                label = "New Password",
                isPassword = true
            )

            Button(
                onClick = onChangePassword,
                enabled = !passwordUpdating,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = appColors.accentOrange),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (passwordUpdating) "Updating..." else "Update Password")
            }
        }
    }
}

@Composable
private fun ProfileInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String? = null,
    isPassword: Boolean = false
) {
    val appColors = DashboardTheme.colors

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = if (placeholder != null) {
            { Text(placeholder) }
        } else {
            null
        },
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

@Composable
fun ThemeSelectorCard(
    selectedTheme: ThemeOption,
    onThemeChange: (ThemeOption) -> Unit
) {
    val appColors = DashboardTheme.colors
    val scrollState = rememberScrollState()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = appColors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ColorLens,
                    contentDescription = null,
                    tint = appColors.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Theme Color",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = appColors.textPrimary
                    )
                    Text(
                        text = "Default is cyan and white. Tap to switch accents.",
                        style = MaterialTheme.typography.bodySmall,
                        color = appColors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ThemeOption.entries.forEach { option ->
                    FilterChip(
                        selected = selectedTheme == option,
                        onClick = { onThemeChange(option) },
                        label = { Text(option.displayName) },
                        leadingIcon = if (selectedTheme == option) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                )
                            }
                        } else {
                            null
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = appColors.primary.copy(alpha = 0.16f),
                            selectedLabelColor = appColors.primary,
                            selectedLeadingIconColor = appColors.primary
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun StatBox(value: String, label: String) {
    val appColors = DashboardTheme.colors

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = appColors.accentGold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun ProfileMenuItem(item: ProfileMenuEntry) {
    val appColors = DashboardTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = appColors.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = appColors.textPrimary
            )
            if (item.subtitle.isNotEmpty()) {
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = appColors.textSecondary
                )
            }
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = appColors.textLight
        )
    }
}

fun profileMenuItems(): List<ProfileMenuEntry> = listOf(
    ProfileMenuEntry(Icons.Default.Settings, "App Settings", "Theme, notifications, and more"),
    ProfileMenuEntry(Icons.Default.Security, "Privacy & Security", "Manage your data"),
    ProfileMenuEntry(Icons.Default.Backup, "Backup & Restore", "Keep your data safe"),
    ProfileMenuEntry(Icons.Default.Info, "About", "Version 1.0.0"),
    ProfileMenuEntry(Icons.AutoMirrored.Filled.Help, "Help & Support", "Get help using the app")
)
