package com.abc.locusvisionis.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.abc.locusvisionis.data.bible.BibleContentEnglish
import com.abc.locusvisionis.data.bible.BibleContentItem
import com.abc.locusvisionis.data.database.AppDatabase
import com.abc.locusvisionis.data.firebase.MoneyDashboardState
import com.abc.locusvisionis.data.firebase.MoneyEntryType
import com.abc.locusvisionis.data.firebase.MoneyManagerRepository
import com.abc.locusvisionis.data.firebase.MoneyTransactionRecord
import com.abc.locusvisionis.data.models.Reflection
import com.abc.locusvisionis.ui.components.DashboardCard
import com.abc.locusvisionis.ui.components.GradientCard
import com.abc.locusvisionis.ui.components.StatItem
import com.abc.locusvisionis.ui.theme.DashboardTheme
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class PublicReflectionPreview(
    val id: String,
    val authorName: String,
    val title: String,
    val content: String,
    val mood: String,
    val publishedAtMillis: Long
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    currentUser: FirebaseUser?,
    firestore: FirebaseFirestore
) {
    val scrollState = rememberScrollState()
    val appColors = DashboardTheme.colors
    val context = LocalContext.current
    val database = remember(context) { AppDatabase.getDatabase(context) }
    val reflectionDao = remember(database) { database.reflectionDao() }
    val reflections by reflectionDao.observeAll().collectAsState(initial = emptyList())
    val bibleVerses = remember { BibleContentEnglish.verses }
    val latestLocalReflection = reflections.firstOrNull()
    val continueReadingVerse = remember(bibleVerses) {
        bibleVerses.firstOrNull { !it.isFavorite } ?: bibleVerses.firstOrNull()
    }
    val moneyRepository = remember(firestore) { MoneyManagerRepository(firestore) }

    var moneyDashboardState by remember { mutableStateOf(MoneyDashboardState()) }
    var moneyLoading by remember { mutableStateOf(true) }

    DisposableEffect(currentUser?.uid, moneyRepository) {
        val uid = currentUser?.uid
        if (uid.isNullOrBlank()) {
            moneyDashboardState = MoneyDashboardState()
            moneyLoading = false
            onDispose { }
        } else {
            moneyLoading = true
            val registration = moneyRepository.observeDashboard(
                userUid = uid,
                onStateChange = { state ->
                    moneyDashboardState = state
                    moneyLoading = false
                },
                onError = {
                    moneyLoading = false
                }
            )

            onDispose { registration.remove() }
        }
    }

    var publicReflections by remember { mutableStateOf<List<PublicReflectionPreview>>(emptyList()) }
    var selectedAuthorName by remember { mutableStateOf<String?>(null) }
    var publicReflectionsLoading by remember { mutableStateOf(true) }

    DisposableEffect(firestore) {
        publicReflectionsLoading = true
        val registration = firestore.collection("public_reflections")
            .orderBy("publishedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                publicReflections = snapshot?.documents.orEmpty().mapNotNull { document ->
                    val authorName = document.getString("authorName").orEmpty().trim()
                    val content = document.getString("content").orEmpty().trim()
                    if (authorName.isBlank() || content.isBlank()) {
                        return@mapNotNull null
                    }

                    PublicReflectionPreview(
                        id = document.id,
                        authorName = authorName,
                        title = document.getString("title").orEmpty().trim(),
                        content = content,
                        mood = document.getString("mood").orEmpty(),
                        publishedAtMillis = document.getTimestamp("publishedAt")?.toDate()?.time ?: 0L
                    )
                }
                val authorNames = publicReflections
                    .map { it.authorName }
                    .distinct()

                selectedAuthorName = when {
                    authorNames.isEmpty() -> null
                    selectedAuthorName in authorNames -> selectedAuthorName
                    else -> authorNames.first()
                }
                publicReflectionsLoading = false
            }

        onDispose { registration.remove() }
    }

    val selectedPublicReflection = remember(publicReflections, selectedAuthorName) {
        publicReflections.firstOrNull { it.authorName == selectedAuthorName }
    }
    val latestTransactions = remember(moneyDashboardState.transactions) {
        moneyDashboardState.transactions.take(3)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        GradientCard(gradientColors = listOf(appColors.gradientStart, appColors.gradientEnd)) {
            Text(
                text = "Welcome Back!",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault()).format(Date()),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.9f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Latest public reflection",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.75f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            when {
                publicReflectionsLoading -> {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                }

                publicReflections.isEmpty() -> {
                    Text(
                        text = "No public reflections have been published yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }

                else -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        publicReflections
                            .map { it.authorName }
                            .distinct()
                            .forEach { authorName ->
                                FilterChip(
                                    selected = selectedAuthorName == authorName,
                                    onClick = { selectedAuthorName = authorName },
                                    label = { Text(authorName) }
                                )
                            }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = selectedPublicReflection?.title?.ifBlank {
                            "${selectedPublicReflection.authorName}'s reflection"
                        } ?: "Reflection preview",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = selectedPublicReflection?.content.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!selectedPublicReflection?.mood.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${selectedPublicReflection?.mood} ${selectedPublicReflection?.authorName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Today's Overview",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = appColors.textPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                icon = Icons.Default.AccountBalanceWallet,
                label = "Balance",
                value = if (moneyLoading) "..." else moneyDashboardState.totalBalance.asCurrency(),
                color = appColors.accentGreen
            )
            StatItem(
                icon = Icons.Default.SelfImprovement,
                label = "Reflections",
                value = reflections.size.toString(),
                color = appColors.accentPurple
            )
            StatItem(
                icon = Icons.AutoMirrored.Filled.MenuBook,
                label = "Verses",
                value = bibleVerses.size.toString(),
                color = appColors.accentOrange
            )
            StatItem(
                icon = Icons.Default.EmojiEvents,
                label = "Goals",
                value = "85%",
                color = appColors.accentGold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = appColors.textPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        DashboardCard(
            title = "Money Manager",
            value = "Track Expenses",
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            iconColor = appColors.accentGreen,
            subtitle = latestTransactions.toQuickFinanceSummary()
        )

        Spacer(modifier = Modifier.height(12.dp))

        DashboardCard(
            title = "Daily Reflection",
            value = latestLocalReflection?.title?.ifBlank { "Latest Reflection" } ?: "Write Today's Entry",
            icon = Icons.Default.Edit,
            iconColor = appColors.accentPurple,
            subtitle = latestLocalReflection?.toQuickReflectionSummary()
                ?: "Record your thoughts and feelings"
        )

        Spacer(modifier = Modifier.height(12.dp))

        DashboardCard(
            title = "Bible Study",
            value = "Continue Reading",
            icon = Icons.AutoMirrored.Filled.MenuBook,
            iconColor = appColors.accentOrange,
            subtitle = continueReadingVerse?.toQuickVerseSummary()
                ?: "Open the Bible section to continue reading"
        )
    }
}

private fun List<MoneyTransactionRecord>.toQuickFinanceSummary(): String {
    if (isEmpty()) {
        return "No income or expense entries yet"
    }

    return joinToString(" | ") { transaction ->
        val prefix = if (transaction.type == MoneyEntryType.INCOME) "+" else "-"
        val label = transaction.description.ifBlank { transaction.category.ifBlank { transaction.walletName } }
        "$prefix${transaction.amount.asCurrency()} $label"
    }
}

private fun Reflection.toQuickReflectionSummary(): String {
    val preview = content.replace("\n", " ").trim()
    return if (preview.length <= 72) {
        preview
    } else {
        "${preview.take(69).trimEnd()}..."
    }
}

private fun BibleContentItem.toQuickVerseSummary(): String {
    val preview = text.trim()
    val shortText = if (preview.length <= 68) preview else "${preview.take(65).trimEnd()}..."
    return "$reference - $shortText"
}

private fun Double.asCurrency(): String {
    val locale = Locale.Builder()
        .setLanguage("in")
        .setRegion("ID")
        .build()
    return NumberFormat.getCurrencyInstance(locale).format(this)
}
