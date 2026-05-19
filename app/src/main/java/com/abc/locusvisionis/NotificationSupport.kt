package com.abc.locusvisionis

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import com.abc.locusvisionis.data.firebase.AppNotificationRecord
import com.abc.locusvisionis.data.firebase.AppNotificationRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging

const val FINANCE_NOTIFICATION_CHANNEL_ID = "finance_updates"
private const val FINANCE_NOTIFICATION_CHANNEL_NAME = "Finance Updates"
private const val FINANCE_NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
private const val USERS_COLLECTION = "users"
private const val FCM_PREFS_NAME = "finance_notifications"
private const val FCM_PENDING_TOKEN_KEY = "pending_fcm_token"
private const val FCM_TOKENS_FIELD = "fcmTokens"

fun Context.ensureFinanceNotificationChannel() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val channel = NotificationChannel(
        FINANCE_NOTIFICATION_CHANNEL_ID,
        FINANCE_NOTIFICATION_CHANNEL_NAME,
        NotificationManager.IMPORTANCE_DEFAULT
    ).apply {
        description = "Alerts when linked wallets receive new income or expense entries."
    }

    notificationManager.createNotificationChannel(channel)
}

fun MainActivity.requestNotificationPermissionIfNeeded() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val alreadyGranted = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.POST_NOTIFICATIONS
    ) == PackageManager.PERMISSION_GRANTED

    if (!alreadyGranted) {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            FINANCE_NOTIFICATION_PERMISSION_REQUEST_CODE
        )
    }
}

fun Context.showFinanceNotification(notification: AppNotificationRecord) {
    val hasPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

    if (!hasPermission) return

    val launchIntent = Intent(this, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

    val pendingIntent = PendingIntent.getActivity(
        this,
        notification.id.hashCode(),
        launchIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val appNotification = NotificationCompat.Builder(this, FINANCE_NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(R.mipmap.locus_visionus)
        .setContentTitle(notification.title)
        .setContentText(notification.body)
        .setStyle(NotificationCompat.BigTextStyle().bigText(notification.body))
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .build()

    NotificationManagerCompat.from(this).notify(notification.id.hashCode(), appNotification)
}

fun Context.isAppInForeground(): Boolean {
    return ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
}

fun Context.syncFcmTokenForUser(userUid: String) {
    val pendingToken = notificationPrefs().getString(FCM_PENDING_TOKEN_KEY, null)
    if (!pendingToken.isNullOrBlank()) {
        persistFcmToken(userUid, pendingToken)
    }

    FirebaseMessaging.getInstance().token
        .addOnSuccessListener { token ->
            if (!token.isNullOrBlank()) {
                cachePendingFcmToken(token)
                persistFcmToken(userUid, token)
            }
        }
}

fun Context.unregisterFcmTokenForUser(userUid: String) {
    FirebaseMessaging.getInstance().token
        .addOnSuccessListener { token ->
            if (token.isNullOrBlank()) return@addOnSuccessListener

            FirebaseFirestore.getInstance()
                .collection(USERS_COLLECTION)
                .document(userUid)
                .set(
                    mapOf(
                        FCM_TOKENS_FIELD to FieldValue.arrayRemove(token),
                        "lastNotificationTokenRemovedAt" to FieldValue.serverTimestamp()
                    ),
                    SetOptions.merge()
                )
        }
}

fun Context.cachePendingFcmToken(token: String) {
    notificationPrefs().edit().putString(FCM_PENDING_TOKEN_KEY, token).apply()
}

fun Context.handleFinancePush(data: Map<String, String>) {
    val notificationId = data["notificationId"].orEmpty()
    val title = data["title"].orEmpty()
    val body = data["body"].orEmpty()
    val recipientUid = data["recipientUid"].orEmpty()

    if (notificationId.isBlank() || title.isBlank() || body.isBlank()) return
    if (isAppInForeground()) return

    showFinanceNotification(
        AppNotificationRecord(
            id = notificationId,
            recipientUid = recipientUid,
            title = title,
            body = body,
            walletId = data["walletId"].orEmpty(),
            transactionId = data["transactionId"].orEmpty(),
            createdAtMillis = data["createdAtMillis"]?.toLongOrNull() ?: 0L
        )
    )

    AppNotificationRepository(FirebaseFirestore.getInstance()).markAsDelivered(notificationId)
}

private fun Context.persistFcmToken(userUid: String, token: String) {
    FirebaseFirestore.getInstance()
        .collection(USERS_COLLECTION)
        .document(userUid)
        .set(
            mapOf(
                FCM_TOKENS_FIELD to FieldValue.arrayUnion(token),
                "lastNotificationTokenAt" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        )
}

private fun Context.notificationPrefs(): SharedPreferences {
    return getSharedPreferences(FCM_PREFS_NAME, Context.MODE_PRIVATE)
}
