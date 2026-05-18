package com.abc.locusvisionis.data.firebase

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot

data class AppNotificationRecord(
    val id: String,
    val recipientUid: String,
    val title: String,
    val body: String,
    val walletId: String,
    val transactionId: String,
    val createdAtMillis: Long
)

class AppNotificationRepository(
    private val firestore: FirebaseFirestore
) {
    fun observePendingNotifications(
        userUid: String,
        onNotification: (AppNotificationRecord) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration {
        return firestore.collection(NOTIFICATIONS_COLLECTION)
            .whereEqualTo("recipientUid", userUid)
            .whereEqualTo("delivered", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error.localizedMessage ?: "Could not load notifications.")
                    return@addSnapshotListener
                }

                snapshot.toNotifications().forEach(onNotification)
            }
    }

    fun markAsDelivered(notificationId: String) {
        firestore.collection(NOTIFICATIONS_COLLECTION)
            .document(notificationId)
            .update(
                mapOf(
                    "delivered" to true,
                    "deliveredAt" to FieldValue.serverTimestamp()
                )
            )
    }

    companion object {
        const val NOTIFICATIONS_COLLECTION = "notifications"
    }
}

private fun QuerySnapshot?.toNotifications(): List<AppNotificationRecord> {
    return this?.documents.orEmpty().mapNotNull { document ->
        val recipientUid = document.getString("recipientUid").orEmpty()
        val title = document.getString("title").orEmpty()
        val body = document.getString("body").orEmpty()
        if (recipientUid.isBlank() || title.isBlank() || body.isBlank()) {
            return@mapNotNull null
        }

        AppNotificationRecord(
            id = document.id,
            recipientUid = recipientUid,
            title = title,
            body = body,
            walletId = document.getString("walletId").orEmpty(),
            transactionId = document.getString("transactionId").orEmpty(),
            createdAtMillis = document.getTimestamp("createdAt")?.toDate()?.time ?: 0L
        )
    }
}
