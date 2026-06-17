import * as admin from "firebase-admin";
import {
  onCall,
  HttpsError,
  CallableRequest,
} from "firebase-functions/https";

interface RejectOfferData {
  tripId: string;
  offerId: string;
}

export const rejectOffer = onCall(
  async (request: CallableRequest<RejectOfferData>) => {
    if (!request.auth || request.auth.token["role"] !== "DRIVER") {
      throw new HttpsError("permission-denied", "Drivers only");
    }

    const { offerId } = request.data;
    if (!offerId) {
      throw new HttpsError("invalid-argument", "Missing offerId");
    }

    const db = admin.firestore();
    const uid = request.auth.uid;

    const offerRef = db.collection("trip_offers").doc(offerId);
    const offerSnap = await offerRef.get();

    if (!offerSnap.exists) {
      throw new HttpsError("not-found", "Offer not found");
    }
    const offer = offerSnap.data()!;
    if (offer["driver_id"] !== uid) {
      throw new HttpsError("permission-denied", "Not your offer");
    }
    if (offer["response"] !== "PENDING") {
      return { success: true }; // Already handled
    }

    await admin.firestore().runTransaction(async (tx) => {
      const snap = await tx.get(offerRef);
      if (snap.data()?.["response"] === "PENDING") {
        tx.update(offerRef, { response: "REJECTED" });
      }
    });

    return { success: true };
  }
);
