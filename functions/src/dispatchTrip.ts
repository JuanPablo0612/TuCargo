import * as admin from "firebase-admin";

const OFFER_TIMEOUT_S = 30;
const OFFER_POLL_INTERVAL_MS = 2000;
const DISPATCH_LOCK_TTL_MS = 90_000;

export async function dispatchTrip(tripId: string): Promise<void> {
  const db = admin.firestore();

  // Acquire dispatch lock
  const lockRef = db.collection("dispatch_locks").doc(tripId);
  const now = Date.now();
  const acquired = await db.runTransaction(async (tx) => {
    const snap = await tx.get(lockRef);
    if (snap.exists) {
      const expiresAt = (snap.data()!["expires_at"] as number) ?? 0;
      if (expiresAt > now) return false;
    }
    tx.set(lockRef, { expires_at: now + DISPATCH_LOCK_TTL_MS, trip_id: tripId });
    return true;
  });

  if (!acquired) return;

  try {
    const tripRef = db.collection("trips").doc(tripId);
    const tripSnap = await tripRef.get();
    if (!tripSnap.exists) return;
    const trip = tripSnap.data()!;

    const configSnap = await db.collection("config").doc("system").get();
    const config = configSnap.data() ?? {};
    const maxAttempts: number = (config["max_dispatch_attempts"] as number) ?? 5;

    let attempt = 0;
    while (attempt < maxAttempts) {
      attempt++;

      // Query candidate drivers
      const driversSnap = await db
        .collection("users")
        .where("role", "==", "DRIVER")
        .where("is_verified", "==", true)
        .where("is_online", "==", true)
        .where("availability", "==", "AVAILABLE")
        .get();

      if (driversSnap.empty) {
        await cancelWithNoDriver(db, tripRef, trip["client_id"] as string);
        return;
      }

      // Pick nearest driver by haversine distance
      const originLat = trip["origin_lat"] as number;
      const originLng = trip["origin_lng"] as number;
      let nearest: admin.firestore.QueryDocumentSnapshot | null = null;
      let minDist = Infinity;
      for (const doc of driversSnap.docs) {
        const d = doc.data();
        const dLat = d["last_lat"] as number | undefined;
        const dLng = d["last_lng"] as number | undefined;
        if (dLat == null || dLng == null) continue;
        const dist = haversineKm(originLat, originLng, dLat, dLng);
        if (dist < minDist) {
          minDist = dist;
          nearest = doc;
        }
      }

      if (!nearest) {
        await cancelWithNoDriver(db, tripRef, trip["client_id"] as string);
        return;
      }

      const driver = nearest.data();
      const driverId = nearest.id;
      const fcmToken = driver["fcm_token"] as string | undefined;

      // Create offer
      const offerRef = db.collection("trip_offers").doc();
      const sentAt = Date.now();
      const expiresAt = sentAt + OFFER_TIMEOUT_S * 1000;
      await offerRef.set({
        id: offerRef.id,
        trip_id: tripId,
        driver_id: driverId,
        attempt,
        sent_at: sentAt,
        expires_at: expiresAt,
        response: "PENDING",
        total_price: trip["price_total"],
        commission_fee: trip["commission_fee"],
        distance_km: trip["distance_km"],
        origin_addr: trip["origin_addr"],
        dest_addr: trip["dest_addr"],
      });

      await tripRef.update({ status: "OFFERED" });

      // Send FCM offer to driver
      if (fcmToken) {
        await sendOfferFcm(fcmToken, {
          offerId: offerRef.id,
          tripId,
          driverId,
          attempt: String(attempt),
          sentAt: String(sentAt),
          expiresAt: String(expiresAt),
          totalPrice: String(trip["price_total"]),
          commissionFee: String(trip["commission_fee"]),
          distanceKm: String(trip["distance_km"]),
          originAddr: trip["origin_addr"] as string,
          destAddr: trip["dest_addr"] as string,
        });
      }

      // Poll for driver decision
      const decision = await pollOfferDecision(db, offerRef.id, expiresAt);

      if (decision === "ACCEPTED") {
        return; // acceptOffer function handles the trip update
      }
      // REJECTED or EXPIRED → continue loop
    }

    // Exhausted all attempts
    await cancelWithNoDriver(db, tripRef, trip["client_id"] as string);
  } finally {
    await lockRef.delete().catch(() => undefined);
  }
}

async function pollOfferDecision(
  db: admin.firestore.Firestore,
  offerId: string,
  expiresAt: number
): Promise<"ACCEPTED" | "REJECTED" | "EXPIRED"> {
  const offerRef = db.collection("trip_offers").doc(offerId);
  while (Date.now() < expiresAt + 2000) {
    await new Promise((r) => setTimeout(r, OFFER_POLL_INTERVAL_MS));
    const snap = await offerRef.get();
    const response = snap.data()?.["response"] as string;
    if (response === "ACCEPTED") return "ACCEPTED";
    if (response === "REJECTED") return "REJECTED";
    if (Date.now() >= expiresAt) {
      await offerRef.update({ response: "EXPIRED" }).catch(() => undefined);
      return "EXPIRED";
    }
  }
  return "EXPIRED";
}

async function cancelWithNoDriver(
  db: admin.firestore.Firestore,
  tripRef: admin.firestore.DocumentReference,
  clientId: string
): Promise<void> {
  await tripRef.update({ status: "CANCELLED_NO_DRIVER" });

  const clientSnap = await db.collection("users").doc(clientId).get();
  const fcmToken = clientSnap.data()?.["fcm_token"] as string | undefined;
  if (fcmToken) {
    await admin.messaging().send({
      token: fcmToken,
      data: { event: "NO_DRIVER_AVAILABLE", trip_id: tripRef.id },
    }).catch(() => undefined);
  }
}

async function sendOfferFcm(
  token: string,
  payload: {
    offerId: string;
    tripId: string;
    driverId: string;
    attempt: string;
    sentAt: string;
    expiresAt: string;
    totalPrice: string;
    commissionFee: string;
    distanceKm: string;
    originAddr: string;
    destAddr: string;
  }
): Promise<void> {
  await admin.messaging().send({
    token,
    data: {
      event: "TRIP_OFFER",
      offer_id: payload.offerId,
      trip_id: payload.tripId,
      driver_id: payload.driverId,
      attempt: payload.attempt,
      sent_at: payload.sentAt,
      expires_at: payload.expiresAt,
      total_price: payload.totalPrice,
      commission_fee: payload.commissionFee,
      distance_km: payload.distanceKm,
      origin_addr: payload.originAddr,
      dest_addr: payload.destAddr,
    },
    android: { priority: "high" },
  });
}

function haversineKm(
  lat1: number,
  lng1: number,
  lat2: number,
  lng2: number
): number {
  const R = 6371;
  const dLat = ((lat2 - lat1) * Math.PI) / 180;
  const dLng = ((lng2 - lng1) * Math.PI) / 180;
  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos((lat1 * Math.PI) / 180) *
      Math.cos((lat2 * Math.PI) / 180) *
      Math.sin(dLng / 2) *
      Math.sin(dLng / 2);
  return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
}
