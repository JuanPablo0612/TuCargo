"use strict";
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    var desc = Object.getOwnPropertyDescriptor(m, k);
    if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
      desc = { enumerable: true, get: function() { return m[k]; } };
    }
    Object.defineProperty(o, k2, desc);
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __setModuleDefault = (this && this.__setModuleDefault) || (Object.create ? (function(o, v) {
    Object.defineProperty(o, "default", { enumerable: true, value: v });
}) : function(o, v) {
    o["default"] = v;
});
var __importStar = (this && this.__importStar) || (function () {
    var ownKeys = function(o) {
        ownKeys = Object.getOwnPropertyNames || function (o) {
            var ar = [];
            for (var k in o) if (Object.prototype.hasOwnProperty.call(o, k)) ar[ar.length] = k;
            return ar;
        };
        return ownKeys(o);
    };
    return function (mod) {
        if (mod && mod.__esModule) return mod;
        var result = {};
        if (mod != null) for (var k = ownKeys(mod), i = 0; i < k.length; i++) if (k[i] !== "default") __createBinding(result, mod, k[i]);
        __setModuleDefault(result, mod);
        return result;
    };
})();
Object.defineProperty(exports, "__esModule", { value: true });
exports.dispatchTrip = dispatchTrip;
const admin = __importStar(require("firebase-admin"));
const OFFER_TIMEOUT_S = 30;
const OFFER_POLL_INTERVAL_MS = 2000;
const DISPATCH_LOCK_TTL_MS = 90_000;
async function dispatchTrip(tripId) {
    const db = admin.firestore();
    // Acquire dispatch lock
    const lockRef = db.collection("dispatch_locks").doc(tripId);
    const now = Date.now();
    const acquired = await db.runTransaction(async (tx) => {
        const snap = await tx.get(lockRef);
        if (snap.exists) {
            const expiresAt = snap.data()["expires_at"] ?? 0;
            if (expiresAt > now)
                return false;
        }
        tx.set(lockRef, { expires_at: now + DISPATCH_LOCK_TTL_MS, trip_id: tripId });
        return true;
    });
    if (!acquired)
        return;
    try {
        const tripRef = db.collection("trips").doc(tripId);
        const tripSnap = await tripRef.get();
        if (!tripSnap.exists)
            return;
        const trip = tripSnap.data();
        const configSnap = await db.collection("config").doc("app_config").get();
        const config = configSnap.data() ?? {};
        const maxAttempts = config["max_dispatch_attempts"] ?? 5;
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
                await cancelWithNoDriver(db, tripRef, trip["client_id"]);
                return;
            }
            // Pick nearest driver by haversine distance
            const originLat = trip["origin_lat"];
            const originLng = trip["origin_lng"];
            let nearest = null;
            let minDist = Infinity;
            for (const doc of driversSnap.docs) {
                const d = doc.data();
                const dLat = d["last_lat"];
                const dLng = d["last_lng"];
                if (dLat == null || dLng == null)
                    continue;
                const dist = haversineKm(originLat, originLng, dLat, dLng);
                if (dist < minDist) {
                    minDist = dist;
                    nearest = doc;
                }
            }
            if (!nearest) {
                await cancelWithNoDriver(db, tripRef, trip["client_id"]);
                return;
            }
            const driver = nearest.data();
            const driverId = nearest.id;
            const fcmToken = driver["fcm_token"];
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
                    originAddr: trip["origin_addr"],
                    destAddr: trip["dest_addr"],
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
        await cancelWithNoDriver(db, tripRef, trip["client_id"]);
    }
    finally {
        await lockRef.delete().catch(() => undefined);
    }
}
async function pollOfferDecision(db, offerId, expiresAt) {
    const offerRef = db.collection("trip_offers").doc(offerId);
    while (Date.now() < expiresAt + 2000) {
        await new Promise((r) => setTimeout(r, OFFER_POLL_INTERVAL_MS));
        const snap = await offerRef.get();
        const response = snap.data()?.["response"];
        if (response === "ACCEPTED")
            return "ACCEPTED";
        if (response === "REJECTED")
            return "REJECTED";
        if (Date.now() >= expiresAt) {
            await offerRef.update({ response: "EXPIRED" }).catch(() => undefined);
            return "EXPIRED";
        }
    }
    return "EXPIRED";
}
async function cancelWithNoDriver(db, tripRef, clientId) {
    await tripRef.update({ status: "CANCELLED_NO_DRIVER" });
    const clientSnap = await db.collection("users").doc(clientId).get();
    const fcmToken = clientSnap.data()?.["fcm_token"];
    if (fcmToken) {
        await admin.messaging().send({
            token: fcmToken,
            data: { event: "NO_DRIVER_AVAILABLE", trip_id: tripRef.id },
        }).catch(() => undefined);
    }
}
async function sendOfferFcm(token, payload) {
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
function haversineKm(lat1, lng1, lat2, lng2) {
    const R = 6371;
    const dLat = ((lat2 - lat1) * Math.PI) / 180;
    const dLng = ((lng2 - lng1) * Math.PI) / 180;
    const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.cos((lat1 * Math.PI) / 180) *
            Math.cos((lat2 * Math.PI) / 180) *
            Math.sin(dLng / 2) *
            Math.sin(dLng / 2);
    return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
}
//# sourceMappingURL=dispatchTrip.js.map