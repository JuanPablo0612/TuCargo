import * as admin from "firebase-admin";
import {
  onCall,
  HttpsError,
  CallableRequest,
} from "firebase-functions/v2/https";

interface UpdateTripStatusData {
  tripId: string;
  action: "ARRIVE_PICKUP" | "START_TRIP" | "ARRIVE_DROPOFF";
}

const ACTION_MAP: Record<
  UpdateTripStatusData["action"],
  { fromStatus: string; toStatus: string; timestampField: string; fcmBody: string }
> = {
  ARRIVE_PICKUP: {
    fromStatus: "ACCEPTED",
    toStatus: "AT_PICKUP",
    timestampField: "arrived_pickup_at",
    fcmBody: "¡Tu moto-carguero llegó al origen!",
  },
  START_TRIP: {
    fromStatus: "AT_PICKUP",
    toStatus: "IN_TRANSIT",
    timestampField: "started_at",
    fcmBody: "Tu envío está en camino.",
  },
  ARRIVE_DROPOFF: {
    fromStatus: "IN_TRANSIT",
    toStatus: "AT_DROPOFF",
    timestampField: "arrived_dropoff_at",
    fcmBody: "¡Tu moto-carguero llegó al destino!",
  },
};

export const updateTripStatus = onCall(
  async (request: CallableRequest<UpdateTripStatusData>) => {
    if (!request.auth || request.auth.token["role"] !== "DRIVER") {
      throw new HttpsError("permission-denied", "Drivers only");
    }

    const { tripId, action } = request.data;
    if (!tripId || !action) {
      throw new HttpsError("invalid-argument", "Missing tripId or action");
    }

    const transition = ACTION_MAP[action];
    if (!transition) {
      throw new HttpsError("invalid-argument", "Invalid action");
    }

    const db = admin.firestore();
    const uid = request.auth.uid;
    let clientId = "";

    await db.runTransaction(async (tx) => {
      const tripRef = db.collection("trips").doc(tripId);
      const tripSnap = await tx.get(tripRef);

      if (!tripSnap.exists) {
        throw new HttpsError("not-found", "Trip not found");
      }
      const trip = tripSnap.data()!;

      if (trip["driver_id"] !== uid) {
        throw new HttpsError("permission-denied", "Not your trip");
      }
      if (trip["status"] !== transition.fromStatus) {
        throw new HttpsError("failed-precondition", "TRIP_INVALID_STATE");
      }

      clientId = trip["client_id"] as string;

      tx.update(tripRef, {
        status: transition.toStatus,
        [transition.timestampField]: admin.firestore.FieldValue.serverTimestamp(),
      });
    });

    if (clientId) {
      const clientSnap = await db.collection("users").doc(clientId).get();
      const fcmToken = clientSnap.data()?.["fcm_token"] as string | undefined;
      if (fcmToken) {
        await admin.messaging().send({
          token: fcmToken,
          notification: { body: transition.fcmBody },
          data: { event: "STATUS_UPDATED", trip_id: tripId, status: transition.toStatus },
          android: { priority: "high" },
        }).catch(() => undefined);
      }
    }

    return { success: true };
  }
);
