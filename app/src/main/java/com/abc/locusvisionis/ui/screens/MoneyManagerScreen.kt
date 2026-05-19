package com.abc.locusvisionis.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.abc.locusvisionis.data.firebase.MoneyDashboardState
import com.abc.locusvisionis.data.firebase.MoneyEntryType
import com.abc.locusvisionis.data.firebase.MoneyManagerRepository
import com.abc.locusvisionis.data.firebase.MoneyTransactionRecord
import com.abc.locusvisionis.data.firebase.WalletRecord
import com.abc.locusvisionis.ui.components.GradientCard
import com.abc.locusvisionis.ui.theme.DashboardTheme
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

private enum class ShareTargetType {
    WALLET,
    TRANSACTION
}

private data class ShareTarget(
    val id: String,
    val label: String,
    val type: ShareTargetType
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MoneyManagerScreen(
    currentUser: FirebaseUser?,
    firestore: FirebaseFirestore
) {
    val context = LocalContext.current
    val appColors = DashboardTheme.colors
    val repository = remember(firestore) { MoneyManagerRepository(firestore) }

    var dashboardState by remember { mutableStateOf(MoneyDashboardState()) }
    var isLoading by remember { mutableStateOf(true) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var entryDialogType by remember { mutableStateOf<MoneyEntryType?>(null) }
    var walletDialogTarget by remember { mutableStateOf<WalletRecord?>(null) }
    var showCreateWalletDialog by remember { mutableStateOf(false) }
    var shareTarget by remember { mutableStateOf<ShareTarget?>(null) }

    DisposableEffect(currentUser?.uid, repository) {
        val uid = currentUser?.uid
        if (uid.isNullOrBlank()) {
            dashboardState = MoneyDashboardState()
            isLoading = false
            errorMessage = "Sign in to manage your money data."
            onDispose { }
        } else {
            isLoading = true
            errorMessage = null

            val registration = repository.observeDashboard(
                userUid = uid,
                onStateChange = { state ->
                    dashboardState = state
                    isLoading = false
                },
                onError = { message ->
                    errorMessage = message
                    isLoading = false
                }
            )

            onDispose { registration.remove() }
        }
    }

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
                text = dashboardState.totalBalance.asCurrency(),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryColumn("Income", dashboardState.totalIncome.asCurrency())
                SummaryColumn("Expenses", dashboardState.totalExpense.asCurrency())
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ActionButton(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Add,
                label = "Add Income",
                color = appColors.accentGreen,
                onClick = { entryDialogType = MoneyEntryType.INCOME }
            )
            ActionButton(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Remove,
                label = "Add Expense",
                color = appColors.accentRed,
                onClick = { entryDialogType = MoneyEntryType.EXPENSE }
            )
            ActionButton(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.AccountBalanceWallet,
                label = "Add Wallet",
                color = appColors.primary,
                onClick = { showCreateWalletDialog = true }
            )
            ActionButton(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Edit,
                label = "Edit Balance",
                color = appColors.secondary,
                onClick = {
                    walletDialogTarget = dashboardState.wallets.firstOrNull()
                    if (walletDialogTarget == null) {
                        Toast.makeText(context, "Create a wallet first.", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        if (!errorMessage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = errorMessage.orEmpty(),
                color = appColors.accentRed,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Wallets",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = appColors.textPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = appColors.primary)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (dashboardState.wallets.isEmpty()) {
                    item {
                        EmptyStateCard(
                            title = "No wallets yet",
                            subtitle = "Create your first wallet to start tracking income and expenses."
                        )
                    }
                } else {
                    items(dashboardState.wallets, key = { it.id }) { wallet ->
                        WalletItem(
                            wallet = wallet,
                            currentUid = currentUser?.uid.orEmpty(),
                            onEdit = { walletDialogTarget = wallet },
                            onShare = {
                                shareTarget = ShareTarget(
                                    id = wallet.id,
                                    label = wallet.name,
                                    type = ShareTargetType.WALLET
                                )
                            }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = "Recent Transactions",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = appColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (dashboardState.transactions.isEmpty()) {
                    item {
                        EmptyStateCard(
                            title = "No transactions yet",
                            subtitle = "Add an income or expense and it will appear here."
                        )
                    }
                } else {
                    items(dashboardState.transactions, key = { it.id }) { transaction ->
                        TransactionItem(
                            transaction = transaction,
                            currentUid = currentUser?.uid.orEmpty(),
                            onShare = {
                                shareTarget = ShareTarget(
                                    id = transaction.id,
                                    label = transaction.description.ifBlank { transaction.walletName },
                                    type = ShareTargetType.TRANSACTION
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    val currentUid = currentUser?.uid
    if (entryDialogType != null && !currentUid.isNullOrBlank()) {
        val categorySuggestions = dashboardState.transactions.categorySuggestionsFor(entryDialogType!!)
        AddTransactionDialog(
            type = entryDialogType!!,
            wallets = dashboardState.wallets,
            categorySuggestions = categorySuggestions,
            isSubmitting = isSubmitting,
            onDismiss = { entryDialogType = null },
            onSubmit = { walletId, amount, description, category ->
                isSubmitting = true
                repository.addTransaction(
                    userUid = currentUid,
                    walletId = walletId,
                    amount = amount,
                    description = description,
                    category = category,
                    type = entryDialogType!!,
                    onSuccess = {
                        isSubmitting = false
                        entryDialogType = null
                        Toast.makeText(context, "Transaction saved.", Toast.LENGTH_SHORT).show()
                    },
                    onError = { message ->
                        isSubmitting = false
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }

    if (showCreateWalletDialog && !currentUid.isNullOrBlank()) {
        WalletBalanceDialog(
            title = "Create Wallet",
            initialName = "",
            initialBalance = "",
            isSubmitting = isSubmitting,
            allowNameEdit = true,
            onDismiss = { showCreateWalletDialog = false },
            onSubmit = { walletName, balance ->
                isSubmitting = true
                repository.createWallet(
                    userUid = currentUid,
                    walletName = walletName,
                    balance = balance,
                    onSuccess = {
                        isSubmitting = false
                        showCreateWalletDialog = false
                        Toast.makeText(context, "Wallet created.", Toast.LENGTH_SHORT).show()
                    },
                    onError = { message ->
                        isSubmitting = false
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }

    if (walletDialogTarget != null && !currentUid.isNullOrBlank()) {
        val wallet = walletDialogTarget!!
        WalletBalanceDialog(
            title = "Edit Wallet Balance",
            initialName = wallet.name,
            initialBalance = wallet.balance.toPlainMoneyString(),
            isSubmitting = isSubmitting,
            allowNameEdit = false,
            onDismiss = { walletDialogTarget = null },
            onSubmit = { _, balance ->
                isSubmitting = true
                repository.updateWalletBalance(
                    userUid = currentUid,
                    walletId = wallet.id,
                    newBalance = balance,
                    onSuccess = {
                        isSubmitting = false
                        walletDialogTarget = null
                        Toast.makeText(context, "Wallet balance updated.", Toast.LENGTH_SHORT).show()
                    },
                    onError = { message ->
                        isSubmitting = false
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }

    if (shareTarget != null && !currentUid.isNullOrBlank()) {
        ShareItemDialog(
            title = if (shareTarget!!.type == ShareTargetType.WALLET) {
                "Share Wallet"
            } else {
                "Share Transaction"
            },
            label = shareTarget!!.label,
            isSubmitting = isSubmitting,
            onDismiss = { shareTarget = null },
            onSubmit = { email ->
                isSubmitting = true
                val onSuccess: (String) -> Unit = { recipientName ->
                    isSubmitting = false
                    shareTarget = null
                    Toast.makeText(
                        context,
                        "Shared with $recipientName.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                val onError: (String) -> Unit = { message ->
                    isSubmitting = false
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                }

                when (shareTarget!!.type) {
                    ShareTargetType.WALLET -> repository.shareWallet(
                        userUid = currentUid,
                        walletId = shareTarget!!.id,
                        recipientEmail = email,
                        onSuccess = onSuccess,
                        onError = onError
                    )

                    ShareTargetType.TRANSACTION -> repository.shareTransaction(
                        userUid = currentUid,
                        transactionId = shareTarget!!.id,
                        recipientEmail = email,
                        onSuccess = onSuccess,
                        onError = onError
                    )
                }
            }
        )
    }
}

@Composable
private fun SummaryColumn(title: String, value: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.8f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun ActionButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    val appColors = DashboardTheme.colors

    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = appColors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = appColors.textPrimary,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun WalletItem(
    wallet: WalletRecord,
    currentUid: String,
    onEdit: () -> Unit,
    onShare: () -> Unit
) {
    val appColors = DashboardTheme.colors
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = appColors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = wallet.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = appColors.textPrimary
                    )
                    Text(
                        text = wallet.balance.asCurrency(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = appColors.textSecondary
                    )
                }
                Text(
                    text = if (wallet.ownerUid == currentUid) "Owned" else "Shared",
                    color = if (wallet.ownerUid == currentUid) appColors.primary else appColors.secondary,
                    style = MaterialTheme.typography.labelMedium
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallActionButton(label = "Edit Balance", icon = Icons.Default.Edit, onClick = onEdit)
                if (wallet.ownerUid == currentUid) {
                    SmallActionButton(label = "Share", icon = Icons.Default.Share, onClick = onShare)
                }
            }
        }
    }
}

@Composable
private fun TransactionItem(
    transaction: MoneyTransactionRecord,
    currentUid: String,
    onShare: () -> Unit
) {
    val appColors = DashboardTheme.colors
    val color = if (transaction.type == MoneyEntryType.INCOME) appColors.accentGreen else appColors.accentRed
    val prefix = if (transaction.type == MoneyEntryType.INCOME) "+" else "-"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = appColors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                            imageVector = if (transaction.type == MoneyEntryType.INCOME) {
                                Icons.AutoMirrored.Filled.TrendingUp
                            } else {
                                Icons.AutoMirrored.Filled.TrendingDown
                            },
                            contentDescription = null,
                            tint = color
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = transaction.description.ifBlank { transaction.category.ifBlank { transaction.walletName } },
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = appColors.textPrimary
                        )
                        Text(
                            text = "${transaction.walletName} • ${transaction.category.ifBlank { "Uncategorized" }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = appColors.textSecondary
                        )
                    }
                }
                Text(
                    text = "$prefix${transaction.amount.asCurrency()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = Date(transaction.createdAtMillis).formatMoneyDate(),
                        style = MaterialTheme.typography.bodySmall,
                        color = appColors.textSecondary
                    )
                    Text(
                        text = if (transaction.ownerUid == currentUid) "Owned" else "Shared",
                        style = MaterialTheme.typography.labelSmall,
                        color = appColors.textLight
                    )
                }
                if (transaction.ownerUid == currentUid) {
                    SmallActionButton(label = "Share", icon = Icons.Default.Share, onClick = onShare)
                }
            }
        }
    }
}

@Composable
private fun SmallActionButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    TextButton(onClick = onClick) {
        Icon(imageVector = icon, contentDescription = null)
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label)
    }
}

@Composable
private fun EmptyStateCard(
    title: String,
    subtitle: String
) {
    val appColors = DashboardTheme.colors
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = appColors.surface)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = appColors.textPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = appColors.textSecondary
            )
        }
    }
}

@Composable
private fun AddTransactionDialog(
    type: MoneyEntryType,
    wallets: List<WalletRecord>,
    categorySuggestions: List<String>,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (String, Double, String, String) -> Unit
) {
    var amount by remember(type) { mutableStateOf("") }
    var description by remember(type) { mutableStateOf("") }
    var category by remember(type) { mutableStateOf("") }
    var selectedWalletId by remember(type, wallets) { mutableStateOf(wallets.firstOrNull()?.id.orEmpty()) }
    var validationMessage by remember(type) { mutableStateOf<String?>(null) }
    val trimmedCategory = category.trim()
    val filteredCategorySuggestions = remember(categorySuggestions, trimmedCategory) {
        categorySuggestions.filteredForCategoryQuery(trimmedCategory)
    }
    val hasExactCategoryMatch = remember(categorySuggestions, trimmedCategory) {
        categorySuggestions.any { it.equals(trimmedCategory, ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = {
            Text(if (type == MoneyEntryType.INCOME) "Add Income" else "Add Expense")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (wallets.isEmpty()) {
                    Text("Create a wallet before adding transactions.")
                } else {
                    MoneyTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = "Amount",
                        keyboardType = KeyboardType.Decimal
                    )
                    MoneyTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = "Description"
                    )
                    MoneyTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = "Category"
                    )
                    if (categorySuggestions.isNotEmpty()) {
                        Text(
                            text = if (trimmedCategory.isBlank()) {
                                if (type == MoneyEntryType.INCOME) {
                                    "Latest income categories"
                                } else {
                                    "Latest expense categories"
                                }
                            } else if (type == MoneyEntryType.INCOME) {
                                "Matching income categories"
                            } else {
                                "Matching expense categories"
                            },
                            style = MaterialTheme.typography.labelLarge
                        )
                        if (filteredCategorySuggestions.isNotEmpty()) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                filteredCategorySuggestions.forEach { suggestion ->
                                    SelectChip(
                                        label = suggestion,
                                        selected = trimmedCategory.equals(suggestion, ignoreCase = true),
                                        onClick = { category = suggestion }
                                    )
                                }
                            }
                        } else if (trimmedCategory.isNotBlank() && !hasExactCategoryMatch) {
                            Text(
                                text = "No saved category matches \"$trimmedCategory\". Save to create it as a new ${type.name.lowercase()} category.",
                                style = MaterialTheme.typography.bodySmall,
                                color = DashboardTheme.colors.textSecondary
                            )
                        }
                    } else if (trimmedCategory.isNotBlank()) {
                        Text(
                            text = "This will create your first ${type.name.lowercase()} category.",
                            style = MaterialTheme.typography.bodySmall,
                            color = DashboardTheme.colors.textSecondary
                        )
                    }
                    Text(
                        text = "Wallet",
                        style = MaterialTheme.typography.labelLarge
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        wallets.forEach { wallet ->
                            SelectChip(
                                label = wallet.name,
                                selected = selectedWalletId == wallet.id,
                                onClick = { selectedWalletId = wallet.id }
                            )
                        }
                    }
                }

                if (!validationMessage.isNullOrBlank()) {
                    Text(
                        text = validationMessage.orEmpty(),
                        color = DashboardTheme.colors.accentRed,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !isSubmitting && wallets.isNotEmpty(),
                onClick = {
                    val parsedAmount = amount.toDoubleOrNull()
                    val message = when {
                        selectedWalletId.isBlank() -> "Choose a wallet."
                        parsedAmount == null || parsedAmount <= 0 -> "Enter a valid amount."
                        description.trim().isBlank() -> "Description is required."
                        category.trim().isBlank() -> "Category is required."
                        else -> null
                    }

                    if (message != null) {
                        validationMessage = message
                    } else {
                        validationMessage = null
                        onSubmit(
                            selectedWalletId,
                            parsedAmount ?: return@Button,
                            description.trim(),
                            category.trim()
                        )
                    }
                }
            ) {
                Text(if (isSubmitting) "Saving..." else "Save")
            }
        },
        dismissButton = {
            TextButton(
                enabled = !isSubmitting,
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun WalletBalanceDialog(
    title: String,
    initialName: String,
    initialBalance: String,
    allowNameEdit: Boolean,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (String, Double) -> Unit
) {
    var walletName by remember(title, initialName) { mutableStateOf(initialName) }
    var balance by remember(title, initialBalance) { mutableStateOf(initialBalance) }
    var validationMessage by remember(title) { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                MoneyTextField(
                    value = walletName,
                    onValueChange = { walletName = it },
                    label = "Wallet Name",
                    enabled = allowNameEdit
                )
                MoneyTextField(
                    value = balance,
                    onValueChange = { balance = it },
                    label = "Balance",
                    keyboardType = KeyboardType.Decimal
                )

                if (!validationMessage.isNullOrBlank()) {
                    Text(
                        text = validationMessage.orEmpty(),
                        color = DashboardTheme.colors.accentRed,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !isSubmitting,
                onClick = {
                    val parsedBalance = balance.toDoubleOrNull()
                    val message = when {
                        walletName.trim().isBlank() -> "Wallet name is required."
                        parsedBalance == null -> "Enter a valid balance."
                        else -> null
                    }

                    if (message != null) {
                        validationMessage = message
                    } else {
                        validationMessage = null
                        onSubmit(walletName.trim(), parsedBalance ?: return@Button)
                    }
                }
            ) {
                Text(if (isSubmitting) "Saving..." else "Save")
            }
        },
        dismissButton = {
            TextButton(
                enabled = !isSubmitting,
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ShareItemDialog(
    title: String,
    label: String,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var email by remember(label) { mutableStateOf("") }
    var validationMessage by remember(label) { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Share \"$label\" with another registered user by email.")
                MoneyTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Recipient Email",
                    keyboardType = KeyboardType.Email
                )

                if (!validationMessage.isNullOrBlank()) {
                    Text(
                        text = validationMessage.orEmpty(),
                        color = DashboardTheme.colors.accentRed,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !isSubmitting,
                onClick = {
                    val trimmedEmail = email.trim()
                    if (trimmedEmail.isBlank()) {
                        validationMessage = "Recipient email is required."
                    } else {
                        validationMessage = null
                        onSubmit(trimmedEmail)
                    }
                }
            ) {
                Text(if (isSubmitting) "Sharing..." else "Share")
            }
        },
        dismissButton = {
            TextButton(
                enabled = !isSubmitting,
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun MoneyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    enabled: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        enabled = enabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun SelectChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val appColors = DashboardTheme.colors
    Card(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(999.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) appColors.primary.copy(alpha = 0.16f) else appColors.cardBackground
        )
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            color = if (selected) appColors.primary else appColors.textPrimary,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

private fun Double.asCurrency(): String {
    val locale = Locale.Builder()
        .setLanguage("in")
        .setRegion("ID")
        .build()
    return NumberFormat.getCurrencyInstance(locale).format(this)
}

private fun Double.toPlainMoneyString(): String {
    return if (this % 1.0 == 0.0) {
        toLong().toString()
    } else {
        toString()
    }
}

private fun Date.formatMoneyDate(): String {
    return SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(this)
}

private fun List<MoneyTransactionRecord>.categorySuggestionsFor(type: MoneyEntryType): List<String> {
    return this.asSequence()
        .filter { it.type == type }
        .map { it.category.trim() }
        .filter { it.isNotBlank() }
        .distinctBy { it.lowercase() }
        .take(8)
        .toList()
}

private fun List<String>.filteredForCategoryQuery(query: String): List<String> {
    if (query.isBlank()) return take(8)

    val normalizedQuery = query.lowercase()
    return this.asSequence()
        .filter { suggestion ->
            suggestion.lowercase().contains(normalizedQuery)
        }
        .take(8)
        .toList()
}
