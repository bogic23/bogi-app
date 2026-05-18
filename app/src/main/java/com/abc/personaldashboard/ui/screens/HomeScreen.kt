package com.abc.personaldashboard.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abc.personaldashboard.ui.components.DashboardCard
import com.abc.personaldashboard.ui.components.GradientCard
import com.abc.personaldashboard.ui.components.StatItem
import com.abc.personaldashboard.ui.theme.DashboardTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val scrollState = rememberScrollState()
    val appColors = DashboardTheme.colors

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
                text = "\"The steadfast love of the Lord never ceases; his mercies never come to an end; they are new every morning.\" - Lamentations 3:22-23",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f)
            )
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
                value = "$12,450",
                color = appColors.accentGreen
            )
            StatItem(
                icon = Icons.Default.SelfImprovement,
                label = "Reflections",
                value = "24",
                color = appColors.accentPurple
            )
            StatItem(
                icon = Icons.Default.MenuBook,
                label = "Verses",
                value = "15",
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
            icon = Icons.Default.TrendingUp,
            iconColor = appColors.accentGreen,
            subtitle = "View your financial summary"
        )

        Spacer(modifier = Modifier.height(12.dp))

        DashboardCard(
            title = "Daily Reflection",
            value = "Write Today's Entry",
            icon = Icons.Default.Edit,
            iconColor = appColors.accentPurple,
            subtitle = "Record your thoughts and feelings"
        )

        Spacer(modifier = Modifier.height(12.dp))

        DashboardCard(
            title = "Bible Study",
            value = "Continue Reading",
            icon = Icons.Default.MenuBook,
            iconColor = appColors.accentOrange,
            subtitle = "John 3:16 - For God so loved the world..."
        )
    }
}
