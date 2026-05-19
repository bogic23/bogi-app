const admin = require("firebase-admin");
const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const logger = require("firebase-functions/logger");

admin.initializeApp();

exports.sendFinanceNotification = onDocumentCreated(
  {
    document: "notifications/{notificationId}",
    region: "us-central1"
  },
  async (event) => {
    const snapshot = event.data;
    if (!snapshot) {
      logger.warn("Notification trigger ran without snapshot data.");
      return;
    }

    const notificationId = event.params.notificationId;
    const payload = snapshot.data();
    const recipientUid = payload.recipientUid;

    if (!recipientUid) {
      logger.warn("Notification is missing recipientUid.", { notificationId });
      return;
    }

    const notificationRef = admin.firestore().collection("notifications").doc(notificationId);
    const latestNotification = await notificationRef.get();
    if (latestNotification.exists && latestNotification.get("delivered") === true) {
      logger.info("Skipping push because notification is already delivered.", { notificationId });
      return;
    }

    const userRef = admin.firestore().collection("users").doc(recipientUid);
    const userSnapshot = await userRef.get();

    if (!userSnapshot.exists) {
      logger.warn("Recipient user document was not found.", { notificationId, recipientUid });
      return;
    }

    const tokens = normalizeTokens(userSnapshot.get("fcmTokens"));
    if (tokens.length === 0) {
      logger.info("Recipient has no FCM tokens registered.", { notificationId, recipientUid });
      return;
    }

    const createdAtMillis =
      snapshot.createTime instanceof Date ? snapshot.createTime.getTime() : Date.now();

    const message = {
      tokens,
      data: {
        notificationId,
        recipientUid,
        title: String(payload.title || "Finance update"),
        body: String(payload.body || ""),
        walletId: String(payload.walletId || ""),
        transactionId: String(payload.transactionId || ""),
        createdAtMillis: String(createdAtMillis)
      },
      android: {
        priority: "high"
      }
    };

    const response = await admin.messaging().sendEachForMulticast(message);
    const invalidTokens = [];

    response.responses.forEach((result, index) => {
      if (result.success) return;

      const code = result.error && result.error.code;
      if (
        code === "messaging/invalid-registration-token" ||
        code === "messaging/registration-token-not-registered"
      ) {
        invalidTokens.push(tokens[index]);
      }
    });

    const updates = {
      pushAttemptedAt: admin.firestore.FieldValue.serverTimestamp(),
      pushSuccessCount: response.successCount,
      pushFailureCount: response.failureCount
    };

    await notificationRef.set(updates, { merge: true });

    if (invalidTokens.length > 0) {
      await userRef.set(
        {
          fcmTokens: admin.firestore.FieldValue.arrayRemove(...invalidTokens)
        },
        { merge: true }
      );
    }
  }
);

function normalizeTokens(value) {
  if (!Array.isArray(value)) {
    return [];
  }

  return [...new Set(value.filter((item) => typeof item === "string" && item.trim().length > 0))];
}
