package com.abc.personaldashboard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abc.personaldashboard.ui.components.GradientCard
import com.abc.personaldashboard.ui.theme.DashboardTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoneyManagerScreen() {
    var showAddDialog by remember { mutableStateOf(false) }
    val appColors = DashboardTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        GradientCard(gradientColors = listOf(appColors.primary, appColors.secondaryVariant)) {
            Text(
                text = "Total Balance",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.9f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$12,450.00",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Income",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "$5,200",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Column {
                    Text(
                        text = "Expenses",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "$3,150",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ActionButton(
                icon = Icons.Default.Add,
                label = "Add Income",
                color = appColors.accentGreen,
                onClick = { showAddDialog = true }
            )
            ActionButton(
                icon = Icons.Default.Remove,
                label = "Add Expense",
                color = appColors.accentRed,
                onClick = { showAddDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Recent Transactions",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = appColors.textPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(getSampleTransactions()) { transaction ->
                TransactionItem(
                    title = transaction.title,
                    amount = transaction.amount,
                    date = transaction.date,
                    type = transaction.type
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun ActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    val appColors = DashboardTheme.colors

    Card(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = appColors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = appColors.textPrimary
            )
        }
    }
}

@Composable
fun TransactionItem(
    title: String,
    amount: String,
    date: String,
    type: String
) {
    val appColors = DashboardTheme.colors
    val color = if (type == "Income") appColors.accentGreen else appColors.accentRed
    val prefix = if (type == "Income") "+" else "-"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = appColors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(color.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (type == "Income") {
                            Icons.Default.TrendingUp
                        } else {
                            Icons.Default.TrendingDown
                        },
                        contentDescription = null,
                        tint = color
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = appColors.textPrimary
                    )
                    Text(
                        text = date,
                        style = MaterialTheme.typography.bodySmall,
                        color = appColors.textSecondary
                    )
                }
            }
            Text(
                text = "$prefix$$amount",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

data class TransactionEntry(
    val title: String,
    val amount: String,
    val date: String,
    val type: String
)

fun getSampleTransactions(): List<TransactionEntry> = listOf(
    TransactionEntry("Salary Deposit", "5,000", "Mar 15, 2024", "Income"),
    TransactionEntry("Grocery Shopping", "150", "Mar 14, 2024", "Expense"),
    TransactionEntry("Freelance Work", "200", "Mar 13, 2024", "Income"),
    TransactionEntry("Netflix Subscription", "15", "Mar 12, 2024", "Expense"),
    TransactionEntry("Investment Return", "350", "Mar 11, 2024", "Income")
)
