package com.abc.locusvisionis

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.abc.locusvisionis.data.firebase.AppNotificationRecord

const val FINANCE_NOTIFICATION_CHANNEL_ID = "finance_updates"
private const val FINANCE_NOTIFICATION_CHANNEL_NAME = "Finance Updates"
private const val FINANCE_NOTIFICATION_PERMISSION_REQUEST_CODE = 1001

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
