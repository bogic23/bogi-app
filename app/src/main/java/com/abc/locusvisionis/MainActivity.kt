package com.abc.locusvisionis

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.abc.locusvisionis.ui.navigation.BottomNavItem
import com.abc.locusvisionis.ui.screens.AuthMode
import com.abc.locusvisionis.ui.screens.AuthScreen
import com.abc.locusvisionis.ui.screens.BibleScreen
import com.abc.locusvisionis.ui.screens.HomeScreen
import com.abc.locusvisionis.ui.screens.MoneyManagerScreen
import com.abc.locusvisionis.ui.screens.ProfileScreen
import com.abc.locusvisionis.ui.screens.ReflectionScreen
import com.abc.locusvisionis.data.firebase.AppNotificationRepository
import com.abc.locusvisionis.ui.theme.DashboardTheme
import com.abc.locusvisionis.ui.theme.PersonalDashboardTheme
import com.abc.locusvisionis.ui.theme.ThemeMode
import com.abc.locusvisionis.ui.theme.ThemeOption
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ensureFinanceNotificationChannel()
        requestNotificationPermissionIfNeeded()
        setContent {
            var themeOption by rememberSaveable { mutableStateOf(ThemeOption.Cyan) }
            var themeMode by rememberSaveable { mutableStateOf(ThemeMode.Light) }

            PersonalDashboardTheme(
                themeOption = themeOption,
                themeMode = themeMode
            ) {
                MainApp(
                    themeOption = themeOption,
                    themeMode = themeMode,
                    onThemeChange = { themeOption = it },
                    onThemeModeChange = { themeMode = it }
                )
            }
        }
    }
}

@Composable
fun MainApp(
    themeOption: ThemeOption,
    themeMode: ThemeMode,
    onThemeChange: (ThemeOption) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit
) {
    val context = LocalContext.current
    val appColors = DashboardTheme.colors
    val firebaseAuth = remember { FirebaseAuth.getInstance() }
    val firestore = remember { FirebaseFirestore.getInstance() }
    val notificationRepository = remember { AppNotificationRepository(firestore) }

    var currentUser by remember { mutableStateOf(firebaseAuth.currentUser) }
    var authLoading by remember { mutableStateOf(true) }
    var authMode by rememberSaveable { mutableStateOf(AuthMode.Login) }
    var fullName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var authError by rememberSaveable { mutableStateOf<String?>(null) }
    var profileDoc by remember { mutableStateOf<UserProfile?>(null) }
    var profileSaving by remember { mutableStateOf(false) }
    var verificationSending by remember { mutableStateOf(false) }
    var passwordUpdating by remember { mutableStateOf(false) }

    fun syncVerificationState(
        user: FirebaseUser,
        onComplete: (() -> Unit)? = null
    ) {
        val payload = hashMapOf(
            "emailVerified" to user.isEmailVerified,
            "email" to (user.email ?: "")
        )

        firestore.collection("users")
            .document(user.uid)
            .set(payload, SetOptions.merge())
            .addOnCompleteListener { onComplete?.invoke() }
    }

    fun refreshUserState(onComplete: (() -> Unit)? = null) {
        val user = firebaseAuth.currentUser
        if (user == null) {
            currentUser = null
            onComplete?.invoke()
            return
        }

        user.reload().addOnCompleteListener {
            val refreshedUser = firebaseAuth.currentUser
            currentUser = refreshedUser

            if (refreshedUser == null) {
                onComplete?.invoke()
            } else {
                syncVerificationState(refreshedUser, onComplete)
            }
        }
    }

    fun reloadProfile() {
        val user = firebaseAuth.currentUser
        if (user == null) {
            currentUser = null
            profileDoc = null
            authLoading = false
            return
        }

        currentUser = user
        authLoading = true
        firestore.collection("users")
            .document(user.uid)
            .get()
            .addOnSuccessListener { snapshot ->
                profileDoc = snapshot.toUserProfile(user.uid, user.isEmailVerified)
                authLoading = false
            }
            .addOnFailureListener { error ->
                profileDoc = null
                authLoading = false
                Toast.makeText(
                    context,
                    error.localizedMessage ?: "Could not load Firestore profile.",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    DisposableEffect(firebaseAuth) {
        val authListener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        firebaseAuth.addAuthStateListener(authListener)
        onDispose { firebaseAuth.removeAuthStateListener(authListener) }
    }

    LaunchedEffect(currentUser?.uid) {
        reloadProfile()
    }

    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { activeUid ->
            context.syncFcmTokenForUser(activeUid)
        }
    }

    DisposableEffect(currentUser?.uid) {
        val activeUid = currentUser?.uid
        if (activeUid.isNullOrBlank()) {
            onDispose { }
        } else {
            val registration = notificationRepository.observePendingNotifications(
                userUid = activeUid,
                onNotification = { notification ->
                    if (context.isAppInForeground()) {
                        context.showFinanceNotification(notification)
                        notificationRepository.markAsDelivered(notification.id)
                    }
                },
                onError = { message ->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            )

            onDispose { registration.remove() }
        }
    }

    when {
        authLoading && currentUser != null && profileDoc == null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = appColors.primary)
            }
        }

        currentUser == null -> {
            AuthScreen(
                authMode = authMode,
                fullName = fullName,
                email = email,
                password = password,
                confirmPassword = confirmPassword,
                isLoading = authLoading,
                errorMessage = authError,
                onModeChange = {
                    authMode = it
                    authError = null
                    password = ""
                    confirmPassword = ""
                },
                onFullNameChange = {
                    fullName = it
                    authError = null
                },
                onEmailChange = {
                    email = it
                    authError = null
                },
                onPasswordChange = {
                    password = it
                    authError = null
                },
                onConfirmPasswordChange = {
                    confirmPassword = it
                    authError = null
                },
                onSubmit = {
                    val trimmedName = fullName.trim()
                    val trimmedEmail = email.trim()
                    val trimmedPassword = password.trim()
                    val trimmedConfirmPassword = confirmPassword.trim()

                    val validationMessage = when {
                        trimmedEmail.isBlank() -> "Email is required."
                        trimmedPassword.isBlank() -> "Password is required."
                        authMode == AuthMode.Register && trimmedName.isBlank() -> "Full name is required."
                        authMode == AuthMode.Register && trimmedPassword.length < 6 -> "Password must be at least 6 characters."
                        authMode == AuthMode.Register && trimmedPassword != trimmedConfirmPassword -> "Passwords do not match."
                        else -> null
                    }

                    if (validationMessage != null) {
                        authError = validationMessage
                    } else {
                        authLoading = true

                        if (authMode == AuthMode.Login) {
                            firebaseAuth.signInWithEmailAndPassword(trimmedEmail, trimmedPassword)
                                .addOnSuccessListener { result ->
                                    val user = result.user
                                    if (user == null) {
                                        authLoading = false
                                        authError = "Login succeeded, but no Firebase user was returned."
                                    } else {
                                        touchUserDocument(
                                            firestore = firestore,
                                            user = user,
                                            fallbackName = user.displayName ?: trimmedEmail.substringBefore("@"),
                                            onSuccess = {
                                                authError = null
                                                password = ""
                                                confirmPassword = ""
                                                refreshUserState {
                                                    reloadProfile()
                                                }
                                            },
                                            onError = { message ->
                                                authLoading = false
                                                authError = message
                                            }
                                        )
                                    }
                                }
                                .addOnFailureListener { error ->
                                    authLoading = false
                                    authError = error.localizedMessage ?: "Login failed."
                                }
                        } else {
                            firebaseAuth.createUserWithEmailAndPassword(trimmedEmail, trimmedPassword)
                                .addOnSuccessListener { result ->
                                    val user = result.user
                                    if (user == null) {
                                        authLoading = false
                                        authError = "Registration succeeded, but no Firebase user was returned."
                                    } else {
                                        user.updateProfile(
                                            userProfileChangeRequest { displayName = trimmedName }
                                        ).addOnCompleteListener {
                                            createUserDocument(
                                                firestore = firestore,
                                                user = user,
                                                fullName = trimmedName,
                                                onSuccess = {
                                                    authError = null
                                                    password = ""
                                                    confirmPassword = ""
                                                    refreshUserState {
                                                        reloadProfile()
                                                    }
                                                },
                                                onError = { message ->
                                                    authLoading = false
                                                    authError = message
                                                }
                                            )
                                        }
                                    }
                                }
                                .addOnFailureListener { error ->
                                    authLoading = false
                                    authError = error.localizedMessage ?: "Registration failed."
                                }
                        }
                    }
                }
            )
        }

        else -> {
            AppScaffold(
                themeOption = themeOption,
                themeMode = themeMode,
                onThemeChange = onThemeChange,
                onThemeModeChange = onThemeModeChange,
                currentUser = currentUser,
                firestore = firestore,
                profileDoc = profileDoc,
                profileSaving = profileSaving,
                verificationSending = verificationSending,
                passwordUpdating = passwordUpdating,
                onSaveProfile = { newFullName, newBirthday ->
                    val user = firebaseAuth.currentUser ?: return@AppScaffold
                    val trimmedName = newFullName.trim()
                    val trimmedBirthday = newBirthday.trim()

                    if (trimmedName.isBlank()) {
                        Toast.makeText(context, "Full name is required.", Toast.LENGTH_SHORT).show()
                        return@AppScaffold
                    }

                    profileSaving = true
                    user.updateProfile(
                        userProfileChangeRequest { displayName = trimmedName }
                    ).addOnCompleteListener {
                        val payload = hashMapOf(
                            "uid" to user.uid,
                            "fullName" to trimmedName,
                            "email" to (user.email ?: ""),
                            "birthday" to trimmedBirthday,
                            "emailVerified" to user.isEmailVerified,
                            "lastLoginAt" to FieldValue.serverTimestamp()
                        )

                        firestore.collection("users")
                            .document(user.uid)
                            .set(payload, SetOptions.merge())
                            .addOnSuccessListener {
                                profileSaving = false
                                refreshUserState {
                                    reloadProfile()
                                    Toast.makeText(context, "Profile updated.", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .addOnFailureListener { error ->
                                profileSaving = false
                                Toast.makeText(
                                    context,
                                    error.localizedMessage ?: "Could not save profile.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }
                },
                onSendVerificationEmail = {
                    val user = firebaseAuth.currentUser ?: return@AppScaffold
                    verificationSending = true
                    user.sendEmailVerification()
                        .addOnSuccessListener {
                            verificationSending = false
                            Toast.makeText(
                                context,
                                "Verification email sent to ${user.email.orEmpty()}",
                                Toast.LENGTH_LONG
                            ).show()
                            refreshUserState {
                                reloadProfile()
                            }
                        }
                        .addOnFailureListener { error ->
                            verificationSending = false
                            Toast.makeText(
                                context,
                                error.localizedMessage ?: "Could not send verification email.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                },
                onRefreshVerification = {
                    refreshUserState {
                        reloadProfile()
                        Toast.makeText(context, "Verification status refreshed.", Toast.LENGTH_SHORT).show()
                    }
                },
                onChangePassword = { currentPasswordValue, newPasswordValue ->
                    val user = firebaseAuth.currentUser ?: return@AppScaffold
                    val userEmail = user.email.orEmpty()
                    val trimmedCurrent = currentPasswordValue.trim()
                    val trimmedNew = newPasswordValue.trim()

                    if (userEmail.isBlank()) {
                        Toast.makeText(context, "This account has no email address.", Toast.LENGTH_LONG).show()
                        return@AppScaffold
                    }
                    if (trimmedCurrent.isBlank()) {
                        Toast.makeText(context, "Current password is required.", Toast.LENGTH_SHORT).show()
                        return@AppScaffold
                    }
                    if (trimmedNew.length < 6) {
                        Toast.makeText(context, "New password must be at least 6 characters.", Toast.LENGTH_LONG).show()
                        return@AppScaffold
                    }

                    passwordUpdating = true
                    val credential = EmailAuthProvider.getCredential(userEmail, trimmedCurrent)
                    user.reauthenticate(credential)
                        .addOnSuccessListener {
                            user.updatePassword(trimmedNew)
                                .addOnSuccessListener {
                                    passwordUpdating = false
                                    Toast.makeText(context, "Password updated.", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { error ->
                                    passwordUpdating = false
                                    Toast.makeText(
                                        context,
                                        error.localizedMessage ?: "Could not update password.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                        }
                        .addOnFailureListener { error ->
                            passwordUpdating = false
                            Toast.makeText(
                                context,
                                error.localizedMessage ?: "Re-authentication failed.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                },
                onSignOut = {
                    firebaseAuth.currentUser?.uid?.let { activeUid ->
                        context.unregisterFcmTokenForUser(activeUid)
                    }
                    firebaseAuth.signOut()
                    profileDoc = null
                    authLoading = false
                    authError = null
                    password = ""
                    confirmPassword = ""
                    Toast.makeText(context, "Signed out", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppScaffold(
    themeOption: ThemeOption,
    themeMode: ThemeMode,
    onThemeChange: (ThemeOption) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    currentUser: FirebaseUser?,
    firestore: FirebaseFirestore,
    profileDoc: UserProfile?,
    profileSaving: Boolean,
    verificationSending: Boolean,
    passwordUpdating: Boolean,
    onSaveProfile: (String, String) -> Unit,
    onSendVerificationEmail: () -> Unit,
    onRefreshVerification: () -> Unit,
    onChangePassword: (String, String) -> Unit,
    onSignOut: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val appColors = DashboardTheme.colors

    Scaffold(
        containerColor = appColors.background,
        bottomBar = {
            NavigationBar(
                containerColor = appColors.surface,
                tonalElevation = 8.dp
            ) {
                val items = listOf(
                    BottomNavItem.Home,
                    BottomNavItem.MoneyManager,
                    BottomNavItem.Reflection,
                    BottomNavItem.Bible,
                    BottomNavItem.Profile
                )

                items.forEach { item ->
                    val selected = currentRoute == item.route
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title
                            )
                        },
                        label = {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = appColors.primary,
                            selectedTextColor = appColors.primary,
                            unselectedIconColor = appColors.textSecondary,
                            unselectedTextColor = appColors.textSecondary,
                            indicatorColor = appColors.primary.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(appColors.background)
                .padding(paddingValues)
        ) {
            NavHost(
                navController = navController,
                startDestination = BottomNavItem.Home.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(BottomNavItem.Home.route) {
                    HomeScreen(
                        currentUser = currentUser,
                        firestore = firestore
                    )
                }
                composable(BottomNavItem.MoneyManager.route) {
                    MoneyManagerScreen(
                        currentUser = currentUser,
                        firestore = firestore
                    )
                }
                composable(BottomNavItem.Reflection.route) {
                    ReflectionScreen(
                        currentUser = currentUser,
                        firestore = firestore
                    )
                }
                composable(BottomNavItem.Bible.route) { BibleScreen() }
                composable(BottomNavItem.Profile.route) {
                    ProfileScreen(
                        user = currentUser,
                        profile = profileDoc,
                        selectedTheme = themeOption,
                        selectedThemeMode = themeMode,
                        onThemeChange = onThemeChange,
                        onThemeModeChange = onThemeModeChange,
                        profileSaving = profileSaving,
                        verificationSending = verificationSending,
                        passwordUpdating = passwordUpdating,
                        onSaveProfile = onSaveProfile,
                        onSendVerificationEmail = onSendVerificationEmail,
                        onRefreshVerification = onRefreshVerification,
                        onChangePassword = onChangePassword,
                        onSignOut = onSignOut
                    )
                }
            }

            DraggableThemeButton(
                selectedTheme = themeOption,
                selectedThemeMode = themeMode,
                onThemeChange = onThemeChange,
                onThemeModeChange = onThemeModeChange,
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }
    }
}

@Composable
private fun DraggableThemeButton(
    selectedTheme: ThemeOption,
    selectedThemeMode: ThemeMode,
    onThemeChange: (ThemeOption) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val appColors = DashboardTheme.colors
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    var dragOffset by rememberSaveable(stateSaver = OffsetSaver) { mutableStateOf(Offset.Zero) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset {
                    IntOffset(
                        x = dragOffset.x.roundToInt(),
                        y = dragOffset.y.roundToInt()
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        dragOffset = Offset(
                            dragOffset.x + dragAmount.x,
                            dragOffset.y + dragAmount.y
                        )
                    }
                }
                .padding(20.dp),
            horizontalAlignment = Alignment.End
        ) {
            if (isExpanded) {
                Card(
                    modifier = Modifier
                        .widthIn(max = 280.dp)
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = appColors.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DragIndicator,
                                contentDescription = null,
                                tint = appColors.textSecondary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Theme Switcher",
                                style = MaterialTheme.typography.titleMedium,
                                color = appColors.textPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Mode",
                            style = MaterialTheme.typography.labelLarge,
                            color = appColors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)) {
                            ThemeMode.entries.forEach { mode ->
                                FilterChip(
                                    selected = selectedThemeMode == mode,
                                    onClick = { onThemeModeChange(mode) },
                                    label = { Text(mode.displayName) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = if (mode == ThemeMode.Dark) {
                                                Icons.Default.DarkMode
                                            } else {
                                                Icons.Default.LightMode
                                            },
                                            contentDescription = null,
                                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = appColors.primary.copy(alpha = 0.18f),
                                        selectedLabelColor = appColors.primary,
                                        selectedLeadingIconColor = appColors.primary
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Color",
                            style = MaterialTheme.typography.labelLarge,
                            color = appColors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)) {
                            ThemeOption.entries.forEach { option ->
                                FilterChip(
                                    selected = selectedTheme == option,
                                    onClick = { onThemeChange(option) },
                                    label = {
                                        Text(
                                            text = option.displayName,
                                            maxLines = 1,
                                            softWrap = false,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
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
                                        selectedContainerColor = appColors.primary.copy(alpha = 0.18f),
                                        selectedLabelColor = appColors.primary,
                                        selectedLeadingIconColor = appColors.primary
                                    )
                                )
                            }
                        }
                    }
                }
            }

            FloatingActionButton(
                onClick = { isExpanded = !isExpanded },
                containerColor = appColors.primary,
                contentColor = if (selectedThemeMode == ThemeMode.Dark) appColors.background else Color.White,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = "Toggle theme controls"
                )
            }
        }
    }
}

private val OffsetSaver = Saver<Offset, List<Float>>(
    save = { listOf(it.x, it.y) },
    restore = { saved -> Offset(saved[0], saved[1]) }
)

data class UserProfile(
    val uid: String,
    val fullName: String,
    val email: String,
    val birthday: String,
    val emailVerified: Boolean,
    val createdAt: String,
    val lastLoginAt: String
)

data class FirebaseProfileState(
    val displayName: String,
    val secondaryText: String,
    val providerLabel: String,
    val actionLabel: String,
    val initials: String,
    val isEmailVerified: Boolean,
    val birthday: String,
    val stats: List<Pair<String, String>>,
    val details: List<Pair<String, String>>
)

fun FirebaseUser?.toProfileState(profile: UserProfile?): FirebaseProfileState {
    if (this == null) {
        return FirebaseProfileState(
            displayName = "Not signed in",
            secondaryText = "Create an account to personalize this dashboard.",
            providerLabel = "Firebase session unavailable",
            actionLabel = "Sign Out",
            initials = "?",
            isEmailVerified = false,
            birthday = "",
            stats = listOf(
                "0" to "Sessions",
                "0" to "Verified",
                "--" to "Status"
            ),
            details = listOf(
                "Email" to "--",
                "Birthday" to "--",
                "Last Login" to "--"
            )
        )
    }

    val primaryProvider = providerData
        .firstOrNull { it.providerId != "firebase" }
        ?.providerId
        ?.replaceFirstChar { it.uppercase() }
        ?: "Password"

    val visibleName = when {
        !displayName.isNullOrBlank() -> displayName!!
        !profile?.fullName.isNullOrBlank() -> profile?.fullName.orEmpty()
        !email.isNullOrBlank() -> email!!.substringBefore("@")
        else -> "Firebase User"
    }

    val subtitle = profile?.email?.takeIf { it.isNotBlank() } ?: email ?: uid
    val birthday = profile?.birthday.orEmpty()
    val verified = profile?.emailVerified ?: isEmailVerified
    val initials = visibleName
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifBlank { "U" }

    return FirebaseProfileState(
        displayName = visibleName,
        secondaryText = subtitle,
        providerLabel = "Signed in with $primaryProvider",
        actionLabel = "Sign Out",
        initials = initials,
        isEmailVerified = verified,
        birthday = birthday,
        stats = listOf(
            ("1" to "Session"),
            ((if (verified) "Yes" else "No") to "Verified"),
            ("Live" to "Status")
        ),
        details = listOf(
            ("Email" to subtitle),
            ("Birthday" to birthday.ifBlank { "Not set" }),
            ("Created" to profile?.createdAt.orEmpty().ifBlank { "Pending" }),
            ("Last Login" to profile?.lastLoginAt.orEmpty().ifBlank { "Just now" })
        )
    )
}

private fun createUserDocument(
    firestore: FirebaseFirestore,
    user: FirebaseUser,
    fullName: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val payload = hashMapOf(
        "uid" to user.uid,
        "fullName" to fullName,
        "email" to (user.email ?: ""),
        "birthday" to "",
        "emailVerified" to user.isEmailVerified,
        "createdAt" to FieldValue.serverTimestamp(),
        "lastLoginAt" to FieldValue.serverTimestamp()
    )

    firestore.collection("users")
        .document(user.uid)
        .set(payload)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { error ->
            onError(error.localizedMessage ?: "Could not create Firestore profile.")
        }
}

private fun touchUserDocument(
    firestore: FirebaseFirestore,
    user: FirebaseUser,
    fallbackName: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val payload = hashMapOf(
        "uid" to user.uid,
        "fullName" to (user.displayName ?: fallbackName),
        "email" to (user.email ?: ""),
        "emailVerified" to user.isEmailVerified,
        "lastLoginAt" to FieldValue.serverTimestamp()
    )

    firestore.collection("users")
        .document(user.uid)
        .set(payload, SetOptions.merge())
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { error ->
            onError(error.localizedMessage ?: "Could not update Firestore profile.")
        }
}

private fun DocumentSnapshot.toUserProfile(uid: String, fallbackEmailVerified: Boolean): UserProfile {
    return UserProfile(
        uid = uid,
        fullName = getString("fullName").orEmpty(),
        email = getString("email").orEmpty(),
        birthday = getString("birthday").orEmpty(),
        emailVerified = getBoolean("emailVerified") ?: fallbackEmailVerified,
        createdAt = getTimestamp("createdAt")?.toDate()?.formatDashboardDate().orEmpty(),
        lastLoginAt = getTimestamp("lastLoginAt")?.toDate()?.formatDashboardDate().orEmpty()
    )
}

private fun Date.formatDashboardDate(): String {
    return SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(this)
}
