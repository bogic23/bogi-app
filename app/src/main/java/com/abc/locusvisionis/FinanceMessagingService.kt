package com.abc.locusvisionis

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FinanceMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        cachePendingFcmToken(token)
        FirebaseAuth.getInstance().currentUser?.uid?.let { userUid ->
            syncFcmTokenForUser(userUid)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        handleFinancePush(message.data)
    }
}
