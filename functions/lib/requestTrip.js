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
exports.requestTrip = void 0;
const admin = __importStar(require("firebase-admin"));
const crypto = __importStar(require("crypto"));
const https_1 = require("firebase-functions/https");
exports.requestTrip = (0, https_1.onCall)(async (request) => {
    if (!request.auth || request.auth.token["role"] !== "CLIENT") {
        throw new https_1.HttpsError("permission-denied", "Clients only");
    }
    const { quoteId, cargoDescription, weightConfirmed } = request.data;
    if (!quoteId || typeof quoteId !== "string") {
        throw new https_1.HttpsError("invalid-argument", "Missing quoteId");
    }
    if (!cargoDescription || cargoDescription.trim().length === 0) {
        throw new https_1.HttpsError("invalid-argument", "Missing cargo description");
    }
    if (!weightConfirmed) {
        throw new https_1.HttpsError("invalid-argument", "Weight must be confirmed");
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
        throw new https_1.HttpsError("failed-precondition", "Client has pending debt");
    }
    const deliveryCode = String(crypto.randomInt(1000, 9999));
    let tripId = "";
    await db.runTransaction(async (tx) => {
        const quoteRef = db.collection("quotes").doc(quoteId);
        const quoteSnap = await tx.get(quoteRef);
        if (!quoteSnap.exists) {
            throw new https_1.HttpsError("not-found", "Quote not found");
        }
        const quote = quoteSnap.data();
        if (quote["client_id"] !== uid) {
            throw new https_1.HttpsError("permission-denied", "Not your quote");
        }
        if (quote["consumed"] === true) {
            throw new https_1.HttpsError("failed-precondition", "QUOTE_ALREADY_USED");
        }
        if (quote["valid_until"] < Date.now()) {
            throw new https_1.HttpsError("failed-precondition", "QUOTE_EXPIRED");
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
            price_distance: quote["total_price"] - quote["base_fare"],
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
});
//# sourceMappingURL=requestTrip.js.map