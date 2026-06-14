import * as admin from "firebase-admin";
import * as crypto from "crypto";
import {
  onCall,
  HttpsError,
  CallableRequest,
} from "firebase-functions/v2/https";

interface RequestTripData {
  quoteId: string;
  cargoDescription: string;
  weightConfirmed: boolean;
}

export const requestTrip = onCall(
  async (request: CallableRequest<RequestTripData>) => {
    if (!request.auth || request.auth.token["role"] !== "CLIENT") {
      throw new HttpsError("permission-denied", "Clients only");
    }

    const { quoteId, cargoDescription, weightConfirmed } = request.data;

    if (!quoteId || typeof quoteId !== "string") {
      throw new HttpsError("invalid-argument", "Missing quoteId");
    }
    if (!cargoDescription || cargoDescription.trim().length === 0) {
      throw new HttpsError("invalid-argument", "Missing cargo description");
    }
    if (!weightConfirmed) {
      throw new HttpsError("invalid-argument", "Weight must be confirmed");
    }

    const db = admin.firestore();
    const uid = request.auth.uid;

    // Check for pending debt
    const debtSnap = await db
      .collection("client_debts")
      .where("client_id", "==", uid)
      .where("status", "==", "PENDING")
      .limit(1)
      .get();
    if (!debtSnap.empty) {
      throw new HttpsError("failed-precondition", "Client has pending debt");
    }

    const deliveryCode = String(crypto.randomInt(1000, 9999));
    let tripId = "";

    await db.runTransaction(async (tx) => {
      const quoteRef = db.collection("quotes").doc(quoteId);
      const quoteSnap = await tx.get(quoteRef);

      if (!quoteSnap.exists) {
        throw new HttpsError("not-found", "Quote not found");
      }
      const quote = quoteSnap.data()!;

      if (quote["client_id"] !== uid) {
        throw new HttpsError("permission-denied", "Not your quote");
      }
      if (quote["consumed"] === true) {
        throw new HttpsError("failed-precondition", "QUOTE_ALREADY_USED");
      }
      if ((quote["valid_until"] as number) < Date.now()) {
        throw new HttpsError("failed-precondition", "QUOTE_EXPIRED");
      }

      const tripRef = db.collection("trips").doc();
      tripId = tripRef.id;

      tx.update(quoteRef, { consumed: true });
      tx.set(tripRef, {
        id: tripId,
        quote_id: quoteId,
        client_id: uid,
        driver_id: null,
        status: "REQUESTED",
        origin_lat: quote["origin_lat"],
        origin_lng: quote["origin_lng"],
        origin_addr: quote["origin_addr"],
        dest_lat: quote["dest_lat"],
        dest_lng: quote["dest_lng"],
        dest_addr: quote["dest_addr"],
        distance_km: quote["distance_km"],
        price_base: quote["base_fare"],
        price_distance: (quote["total_price"] as number) - (quote["base_fare"] as number),
        price_total: quote["total_price"],
        commission_fee: quote["commission_fee"],
        cargo_description: cargoDescription.trim(),
        weight_confirmed: weightConfirmed,
        delivery_code: deliveryCode,
        payment_method: "CASH",
        created_at: admin.firestore.FieldValue.serverTimestamp(),
      });
    });

    return { tripId, deliveryCode };
  }
);
