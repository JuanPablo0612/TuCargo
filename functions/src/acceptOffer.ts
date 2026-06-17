import * as admin from "firebase-admin";
import {
  onCall,
  HttpsError,
  CallableRequest,
} from "firebase-functions/https";

interface AcceptOfferData {
  tripId: string;
  offerId: string;
}

export const acceptOffer = onCall(
  async (request: CallableRequest<AcceptOfferData>) => {
    if (!request.auth || request.auth.token["role"] !== "DRIVER") {
      throw new HttpsError("permission-denied", "Drivers only");
    }

    const { tripId, offerId } = request.data;
    if (!tripId || !offerId) {
      throw new HttpsError("invalid-argument", "Missing tripId or offerId");
    }

    const db = admin.firestore();
    const uid = request.auth.uid;
    const now = Date.now();

    let clientId = "";

    await db.runTransaction(async (tx) => {
      const offerRef = db.collection("trip_offers").doc(offerId);
      const tripRef = db.collection("trips").doc(tripId);
      const driverRef = db.collection("users").doc(uid);

      const [offerSnap, tripSnap, driverSnap] = await Promise.all([
        tx.get(offerRef),
        tx.get(tripRef),
        tx.get(driverRef),
      ]);

      if (!offerSnap.exists) throw new HttpsError("not-found", "Offer not found");
      const offer = offerSnap.data()!;
      if (offer["response"] !== "PENDING") {
        throw new HttpsError("failed-precondition", "OFFER_EXPIRED");
      }
      if ((offer["expires_at"] as number) <= now) {
        throw new HttpsError("failed-precondition", "OFFER_EXPIRED");
      }
      if (offer["driver_id"] !== uid) {
        throw new HttpsError("permission-denied", "Not your offer");
      }

      if (!tripSnap.exists) throw new HttpsError("not-found", "Trip not found");
      const trip = tripSnap.data()!;
      if (trip["status"] !== "OFFERED") {
        throw new HttpsError("failed-precondition", "Trip no longer available");
      }

      if (!driverSnap.exists) throw new HttpsError("not-found", "Driver not found");
      const driver = driverSnap.data()!;
      const walletBalance: number = driver["wallet_balance"] ?? 0;
      const configSnap = await db.collection("config").doc("app_config").get();
      const commissionFloor: number =
        (configSnap.data()?.["commission_floor"] as number) ?? 0;
      if (walletBalance < commissionFloor) {
        throw new HttpsError("failed-precondition", "WALLET_INSUFFICIENT");
      }

      clientId = trip["client_id"] as string;

      tx.update(offerRef, { response: "ACCEPTED" });
      tx.update(tripRef, {
        status: "ACCEPTED",
        driver_id: uid,
        driver_name: driver["full_name"] ?? "",
        driver_plate: driver["vehicle"]?.["plate"] ?? "",
        driver_phone: driver["phone"] ?? "",
        accepted_at: admin.firestore.FieldValue.serverTimestamp(),
      });
      tx.update(driverRef, { availability: "ON_TRIP" });
    });

    // Notify client
    if (clientId) {
      const clientSnap = await db.collection("users").doc(clientId).get();
      const fcmToken = clientSnap.data()?.["fcm_token"] as string | undefined;
      if (fcmToken) {
        await admin.messaging().send({
          token: fcmToken,
          data: { event: "TRIP_ACCEPTED", trip_id: tripId },
        }).catch(() => undefined);
      }
    }

    return { success: true };
  }
);
