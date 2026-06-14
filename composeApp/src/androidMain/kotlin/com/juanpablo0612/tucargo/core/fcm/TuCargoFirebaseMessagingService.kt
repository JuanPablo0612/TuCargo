package com.juanpablo0612.tucargo.core.fcm

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class TuCargoFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val data = message.data
        when (data["event"]) {
            "TRIP_OFFER" -> OfferEventBus.postOffer(data)
            "OFFER_CANCELLED" -> {
                val tripId = data["trip_id"] ?: return
                OfferEventBus.postCancellation(tripId)
            }
            // STATUS_UPDATED and TRIP_COMPLETED: Firestore real-time listeners
            // handle UI updates. FCM wakes the app from background so the
            // notification body (set by the Cloud Function) surfaces as a
            // system notification automatically.
            "STATUS_UPDATED", "TRIP_COMPLETED" -> Unit
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Token refresh is handled by the app on next sign-in or foreground resume.
    }
}
