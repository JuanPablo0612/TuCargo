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
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Token refresh is handled by the app on next sign-in or foreground resume.
    }
}
