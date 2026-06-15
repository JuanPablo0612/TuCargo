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
exports.acceptOffer = void 0;
const admin = __importStar(require("firebase-admin"));
const https_1 = require("firebase-functions/v2/https");
exports.acceptOffer = (0, https_1.onCall)(async (request) => {
    if (!request.auth || request.auth.token["role"] !== "DRIVER") {
        throw new https_1.HttpsError("permission-denied", "Drivers only");
    }
    const { tripId, offerId } = request.data;
    if (!tripId || !offerId) {
        throw new https_1.HttpsError("invalid-argument", "Missing tripId or offerId");
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
        if (!offerSnap.exists)
            throw new https_1.HttpsError("not-found", "Offer not found");
        const offer = offerSnap.data();
        if (offer["response"] !== "PENDING") {
            throw new https_1.HttpsError("failed-precondition", "OFFER_EXPIRED");
        }
        if (offer["expires_at"] <= now) {
            throw new https_1.HttpsError("failed-precondition", "OFFER_EXPIRED");
        }
        if (offer["driver_id"] !== uid) {
            throw new https_1.HttpsError("permission-denied", "Not your offer");
        }
        if (!tripSnap.exists)
            throw new https_1.HttpsError("not-found", "Trip not found");
        const trip = tripSnap.data();
        if (trip["status"] !== "OFFERED") {
            throw new https_1.HttpsError("failed-precondition", "Trip no longer available");
        }
        if (!driverSnap.exists)
            throw new https_1.HttpsError("not-found", "Driver not found");
        const driver = driverSnap.data();
        const walletBalance = driver["wallet_balance"] ?? 0;
        const configSnap = await db.collection("config").doc("app_config").get();
        const commissionFloor = configSnap.data()?.["commission_floor"] ?? 0;
        if (walletBalance < commissionFloor) {
            throw new https_1.HttpsError("failed-precondition", "WALLET_INSUFFICIENT");
        }
        clientId = trip["client_id"];
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
        const fcmToken = clientSnap.data()?.["fcm_token"];
        if (fcmToken) {
            await admin.messaging().send({
                token: fcmToken,
                data: { event: "TRIP_ACCEPTED", trip_id: tripId },
            }).catch(() => undefined);
        }
    }
    return { success: true };
});
//# sourceMappingURL=acceptOffer.js.map