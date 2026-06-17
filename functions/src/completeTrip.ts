import * as admin from "firebase-admin";
import * as crypto from "crypto";
import {
  onCall,
  HttpsError,
  CallableRequest,
} from "firebase-functions/https";

interface CompleteTripData {
  tripId: string;
  deliveryCode: string;
}

const MAX_ATTEMPTS = 5;

export const completeTrip = onCall(
  async (request: CallableRequest<CompleteTripData>) => {
    if (!request.auth || request.auth.token["role"] !== "DRIVER") {
      throw new HttpsError("permission-denied", "Drivers only");
    }

    const { tripId, deliveryCode } = request.data;

    if (!tripId) {
      throw new HttpsError("invalid-argument", "Missing tripId");
    }
    if (!deliveryCode || !/^\d{4}$/.test(deliveryCode)) {
      throw new HttpsError("invalid-argument", "INVALID_CODE_FORMAT");
    }

    const db = admin.firestore();
    const uid = request.auth.uid;
    let clientId = "";
    let success = false;

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
      if (trip["status"] !== "AT_DROPOFF") {
        throw new HttpsError("failed-precondition", "TRIP_INVALID_STATE");
      }

      const attempts: number = trip["delivery_code_attempts"] ?? 0;
      if (attempts >= MAX_ATTEMPTS) {
        throw new HttpsError("failed-precondition", "DELIVERY_CODE_LOCKED");
      }

      const storedCode: string = trip["delivery_code"] ?? "";
      const inputBuf = Buffer.from(deliveryCode);
      const storedBuf = Buffer.from(storedCode);

      // Pad to same length so timingSafeEqual doesn't throw on mismatched lengths.
      const maxLen = Math.max(inputBuf.length, storedBuf.length);
      const paddedInput = Buffer.alloc(maxLen);
      const paddedStored = Buffer.alloc(maxLen);
      inputBuf.copy(paddedInput);
      storedBuf.copy(paddedStored);

      const isMatch = crypto.timingSafeEqual(paddedInput, paddedStored);

      clientId = trip["client_id"] as string;

      if (isMatch) {
        success = true;
        tx.update(tripRef, {
          status: "COMPLETED",
          completed_at: admin.firestore.FieldValue.serverTimestamp(),
          delivery_code_verified_at: admin.firestore.FieldValue.serverTimestamp(),
        });
      } else {
        const newAttempts = attempts + 1;
        tx.update(tripRef, { delivery_code_attempts: newAttempts });

        if (newAttempts >= MAX_ATTEMPTS) {
          const auditRef = db.collection("audit_log").doc();
          tx.set(auditRef, {
            action: "DELIVERY_CODE_LOCKED",
            entityType: "trip",
            entityId: tripId,
            createdAt: admin.firestore.FieldValue.serverTimestamp(),
          });
          throw new HttpsError("failed-precondition", "DELIVERY_CODE_LOCKED");
        }

        const remaining = MAX_ATTEMPTS - newAttempts;
        throw new HttpsError(
          "failed-precondition",
          `DELIVERY_CODE_INVALID:${remaining}`
        );
      }
    });

    if (success && clientId) {
      const clientSnap = await db.collection("users").doc(clientId).get();
      const fcmToken = clientSnap.data()?.["fcm_token"] as string | undefined;
      if (fcmToken) {
        await admin.messaging().send({
          token: fcmToken,
          notification: { body: "¡Tu envío fue entregado!" },
          data: { event: "TRIP_COMPLETED", trip_id: tripId },
          android: { priority: "high" },
        }).catch(() => undefined);
      }
    }

    return { success: true };
  }
);
