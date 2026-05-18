package com.abc.locusvisionis.data.firebase

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import java.text.NumberFormat
import java.util.Locale

enum class MoneyEntryType {
    INCOME,
    EXPENSE
}

data class WalletRecord(
    val id: String,
    val ownerUid: String,
    val name: String,
    val balance: Double,
    val sharedWith: List<String>,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
) {
    val isShared: Boolean
        get() = sharedWith.isNotEmpty()
}

data class MoneyTransactionRecord(
    val id: String,
    val ownerUid: String,
    val walletId: String,
    val walletName: String,
    val amount: Double,
    val description: String,
    val category: String,
    val type: MoneyEntryType,
    val sharedWith: List<String>,
    val createdAtMillis: Long,
    val createdByUid: String
)

data class MoneyDashboardState(
    val wallets: List<WalletRecord> = emptyList(),
    val transactions: List<MoneyTransactionRecord> = emptyList(),
    val totalBalance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0
)

class MoneyManagerRepository(
    private val firestore: FirebaseFirestore
) {
    fun observeDashboard(
        userUid: String,
        onStateChange: (MoneyDashboardState) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration {
        var ownedWallets: List<WalletRecord> = emptyList()
        var sharedWallets: List<WalletRecord> = emptyList()
        var ownedTransactions: List<MoneyTransactionRecord> = emptyList()
        var sharedTransactions: List<MoneyTransactionRecord> = emptyList()

        fun publish() {
            val wallets = (ownedWallets + sharedWallets)
                .distinctBy { it.id }
                .sortedWith(compareByDescending<WalletRecord> { it.updatedAtMillis }.thenBy { it.name.lowercase() })

            val transactions = (ownedTransactions + sharedTransactions)
                .distinctBy { it.id }
                .sortedByDescending { it.createdAtMillis }

            onStateChange(
                MoneyDashboardState(
                    wallets = wallets,
                    transactions = transactions,
                    totalBalance = wallets.sumOf { it.balance },
                    totalIncome = transactions
                        .filter { it.type == MoneyEntryType.INCOME }
                        .sumOf { it.amount },
                    totalExpense = transactions
                        .filter { it.type == MoneyEntryType.EXPENSE }
                        .sumOf { it.amount }
                )
            )
        }

        val listeners = listOf(
            firestore.collection(WALLETS_COLLECTION)
                .whereEqualTo("ownerUid", userUid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        onError(error.localizedMessage ?: "Could not load wallets.")
                        return@addSnapshotListener
                    }
                    ownedWallets = snapshot.toWallets()
                    publish()
                },
            firestore.collection(WALLETS_COLLECTION)
                .whereArrayContains("sharedWith", userUid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        onError(error.localizedMessage ?: "Could not load shared wallets.")
                        return@addSnapshotListener
                    }
                    sharedWallets = snapshot.toWallets()
                    publish()
                },
            firestore.collection(TRANSACTIONS_COLLECTION)
                .whereEqualTo("ownerUid", userUid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        onError(error.localizedMessage ?: "Could not load transactions.")
                        return@addSnapshotListener
                    }
                    ownedTransactions = snapshot.toTransactions()
                    publish()
                },
            firestore.collection(TRANSACTIONS_COLLECTION)
                .whereArrayContains("sharedWith", userUid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        onError(error.localizedMessage ?: "Could not load shared transactions.")
                        return@addSnapshotListener
                    }
                    sharedTransactions = snapshot.toTransactions()
                    publish()
                }
        )

        return ListenerRegistration {
            listeners.forEach { it.remove() }
        }
    }

    fun createWallet(
        userUid: String,
        walletName: String,
        balance: Double,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val walletRef = firestore.collection(WALLETS_COLLECTION).document()
        val payload = hashMapOf(
            "ownerUid" to userUid,
            "name" to walletName,
            "balance" to balance,
            "sharedWith" to emptyList<String>(),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        walletRef.set(payload)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { error ->
                onError(error.localizedMessage ?: "Could not create wallet.")
            }
    }

    fun updateWalletBalance(
        userUid: String,
        walletId: String,
        newBalance: Double,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val walletRef = firestore.collection(WALLETS_COLLECTION).document(walletId)
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(walletRef)
            if (!snapshot.exists()) {
                throw IllegalStateException("Wallet not found.")
            }

            val ownerUid = snapshot.getString("ownerUid").orEmpty()
            val sharedWith = snapshot.get("sharedWith") as? List<*> ?: emptyList<Any>()
            val canAccess = ownerUid == userUid || sharedWith.filterIsInstance<String>().contains(userUid)
            if (!canAccess) {
                throw IllegalStateException("You do not have access to update this wallet.")
            }

            transaction.update(
                walletRef,
                mapOf(
                    "balance" to newBalance,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
        }.addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener { error ->
            onError(error.localizedMessage ?: "Could not update wallet balance.")
        }
    }

    fun addTransaction(
        userUid: String,
        walletId: String,
        amount: Double,
        description: String,
        category: String,
        type: MoneyEntryType,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val walletRef = firestore.collection(WALLETS_COLLECTION).document(walletId)
        val transactionRef = firestore.collection(TRANSACTIONS_COLLECTION).document()
        var walletNameForNotification = ""
        var recipientUidsForNotification: List<String> = emptyList()

        firestore.runTransaction { transaction ->
            val walletSnapshot = transaction.get(walletRef)
            if (!walletSnapshot.exists()) {
                throw IllegalStateException("Wallet not found.")
            }

            val ownerUid = walletSnapshot.getString("ownerUid").orEmpty()
            val sharedWith = (walletSnapshot.get("sharedWith") as? List<*>)?.filterIsInstance<String>().orEmpty()
            val canAccess = ownerUid == userUid || sharedWith.contains(userUid)
            if (!canAccess) {
                throw IllegalStateException("You do not have access to this wallet.")
            }

            val walletName = walletSnapshot.getString("name").orEmpty()
            val currentBalance = walletSnapshot.getDouble("balance") ?: 0.0
            val balanceDelta = if (type == MoneyEntryType.INCOME) amount else -amount
            walletNameForNotification = walletName
            recipientUidsForNotification = (listOf(ownerUid) + sharedWith)
                .distinct()
                .filter { it.isNotBlank() && it != userUid }

            transaction.set(
                transactionRef,
                mapOf(
                    "ownerUid" to ownerUid,
                    "walletId" to walletId,
                    "walletName" to walletName,
                    "amount" to amount,
                    "description" to description,
                    "category" to category,
                    "type" to type.name,
                    "sharedWith" to sharedWith,
                    "createdByUid" to userUid,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )

            transaction.set(
                walletRef,
                mapOf(
                    "balance" to currentBalance + balanceDelta,
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )
        }.addOnSuccessListener {
            createWalletActivityNotifications(
                recipientUids = recipientUidsForNotification,
                walletId = walletId,
                walletName = walletNameForNotification,
                transactionId = transactionRef.id,
                amount = amount,
                description = description,
                type = type,
                createdByUid = userUid
            )
            onSuccess()
        }.addOnFailureListener { error ->
            onError(error.localizedMessage ?: "Could not save transaction.")
        }
    }

    private fun createWalletActivityNotifications(
        recipientUids: List<String>,
        walletId: String,
        walletName: String,
        transactionId: String,
        amount: Double,
        description: String,
        type: MoneyEntryType,
        createdByUid: String
    ) {
        if (recipientUids.isEmpty()) return

        val actionLabel = if (type == MoneyEntryType.INCOME) "Income added" else "Expense added"
        val detail = description.ifBlank { "No description" }
        val body = "${amount.asNotificationCurrency()} • $detail • $walletName"
        val notifications = firestore.collection(AppNotificationRepository.NOTIFICATIONS_COLLECTION)
        val batch = firestore.batch()

        recipientUids.forEach { recipientUid ->
            val notificationRef = notifications.document()
            batch.set(
                notificationRef,
                mapOf(
                    "recipientUid" to recipientUid,
                    "title" to actionLabel,
                    "body" to body,
                    "walletId" to walletId,
                    "transactionId" to transactionId,
                    "type" to type.name,
                    "createdByUid" to createdByUid,
                    "delivered" to false,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
        }

        batch.commit()
    }

    fun shareWallet(
        userUid: String,
        walletId: String,
        recipientEmail: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        shareDocumentWithUser(
            collection = WALLETS_COLLECTION,
            userUid = userUid,
            documentId = walletId,
            recipientEmail = recipientEmail,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun shareTransaction(
        userUid: String,
        transactionId: String,
        recipientEmail: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        shareDocumentWithUser(
            collection = TRANSACTIONS_COLLECTION,
            userUid = userUid,
            documentId = transactionId,
            recipientEmail = recipientEmail,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    private fun shareDocumentWithUser(
        collection: String,
        userUid: String,
        documentId: String,
        recipientEmail: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        firestore.collection(USERS_COLLECTION)
            .whereEqualTo("email", recipientEmail)
            .limit(1)
            .get()
            .addOnSuccessListener { userSnapshots ->
                val targetUser = userSnapshots.documents.firstOrNull()
                val recipientUid = targetUser?.getString("uid").orEmpty()
                if (recipientUid.isBlank()) {
                    onError("No registered user was found for that email.")
                    return@addOnSuccessListener
                }
                if (recipientUid == userUid) {
                    onError("You already own this data.")
                    return@addOnSuccessListener
                }

                val documentRef = firestore.collection(collection).document(documentId)
                firestore.runTransaction { transaction ->
                    val snapshot = transaction.get(documentRef)
                    if (!snapshot.exists()) {
                        throw IllegalStateException("Item not found.")
                    }

                    val ownerUid = snapshot.getString("ownerUid").orEmpty()
                    if (ownerUid != userUid) {
                        throw IllegalStateException("Only the owner can share this item.")
                    }

                    transaction.update(
                        documentRef,
                        mapOf(
                            "sharedWith" to FieldValue.arrayUnion(recipientUid),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                }.addOnSuccessListener {
                    onSuccess(targetUser?.getString("fullName").orEmpty().ifBlank { recipientEmail })
                }.addOnFailureListener { error ->
                    onError(error.localizedMessage ?: "Could not share this item.")
                }
            }
            .addOnFailureListener { error ->
                onError(error.localizedMessage ?: "Could not find the recipient user.")
            }
    }

    private fun QuerySnapshot?.toWallets(): List<WalletRecord> {
        return this?.documents.orEmpty().mapNotNull { document ->
            val ownerUid = document.getString("ownerUid").orEmpty()
            val name = document.getString("name").orEmpty()
            if (ownerUid.isBlank() || name.isBlank()) {
                return@mapNotNull null
            }

            WalletRecord(
                id = document.id,
                ownerUid = ownerUid,
                name = name,
                balance = document.getDouble("balance") ?: 0.0,
                sharedWith = (document.get("sharedWith") as? List<*>)?.filterIsInstance<String>().orEmpty(),
                createdAtMillis = document.getTimestamp("createdAt")?.toDate()?.time ?: 0L,
                updatedAtMillis = document.getTimestamp("updatedAt")?.toDate()?.time ?: 0L
            )
        }
    }

    private fun QuerySnapshot?.toTransactions(): List<MoneyTransactionRecord> {
        return this?.documents.orEmpty().mapNotNull { document ->
            val ownerUid = document.getString("ownerUid").orEmpty()
            val walletId = document.getString("walletId").orEmpty()
            val type = document.getString("type")
                ?.let {
                    runCatching { MoneyEntryType.valueOf(it) }.getOrNull()
                }
                ?: return@mapNotNull null

            if (ownerUid.isBlank() || walletId.isBlank()) {
                return@mapNotNull null
            }

            MoneyTransactionRecord(
                id = document.id,
                ownerUid = ownerUid,
                walletId = walletId,
                walletName = document.getString("walletName").orEmpty(),
                amount = document.getDouble("amount") ?: 0.0,
                description = document.getString("description").orEmpty(),
                category = document.getString("category").orEmpty(),
                type = type,
                sharedWith = (document.get("sharedWith") as? List<*>)?.filterIsInstance<String>().orEmpty(),
                createdAtMillis = document.getTimestamp("createdAt")?.toDate()?.time ?: 0L,
                createdByUid = document.getString("createdByUid").orEmpty()
            )
        }
    }

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val WALLETS_COLLECTION = "wallets"
        private const val TRANSACTIONS_COLLECTION = "transactions"
    }
}

private fun Double.asNotificationCurrency(): String {
    val locale = Locale.Builder()
        .setLanguage("id")
        .setRegion("ID")
        .build()
    return NumberFormat.getCurrencyInstance(locale).format(this)
}
