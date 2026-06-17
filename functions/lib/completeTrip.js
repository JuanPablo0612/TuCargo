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
exports.completeTrip = void 0;
const admin = __importStar(require("firebase-admin"));
const crypto = __importStar(require("crypto"));
const https_1 = require("firebase-functions/https");
const MAX_ATTEMPTS = 5;
exports.completeTrip = (0, https_1.onCall)(async (request) => {
    if (!request.auth || request.auth.token["role"] !== "DRIVER") {
        throw new https_1.HttpsError("permission-denied", "Drivers only");
    }
    const { tripId, deliveryCode } = request.data;
    if (!tripId) {
        throw new https_1.HttpsError("invalid-argument", "Missing tripId");
    }
    if (!deliveryCode || !/^\d{4}$/.test(deliveryCode)) {
        throw new https_1.HttpsError("invalid-argument", "INVALID_CODE_FORMAT");
    }
    const db = admin.firestore();
    const uid = request.auth.uid;
    let clientId = "";
    let success = false;
    await db.runTransaction(async (tx) => {
        const tripRef = db.collection("trips").doc(tripId);
        const tripSnap = await tx.get(tripRef);
        if (!tripSnap.exists) {
            throw new https_1.HttpsError("not-found", "Trip not found");
        }
        const trip = tripSnap.data();
        if (trip["driver_id"] !== uid) {
            throw new https_1.HttpsError("permission-denied", "Not your trip");
        }
        if (trip["status"] !== "AT_DROPOFF") {
            throw new https_1.HttpsError("failed-precondition", "TRIP_INVALID_STATE");
        }
        const attempts = trip["delivery_code_attempts"] ?? 0;
        if (attempts >= MAX_ATTEMPTS) {
            throw new https_1.HttpsError("failed-precondition", "DELIVERY_CODE_LOCKED");
        }
        const storedCode = trip["delivery_code"] ?? "";
        const inputBuf = Buffer.from(deliveryCode);
        const storedBuf = Buffer.from(storedCode);
        // Pad to same length so timingSafeEqual doesn't throw on mismatched lengths.
        const maxLen = Math.max(inputBuf.length, storedBuf.length);
        const paddedInput = Buffer.alloc(maxLen);
        const paddedStored = Buffer.alloc(maxLen);
        inputBuf.copy(paddedInput);
        storedBuf.copy(paddedStored);
        const isMatch = crypto.timingSafeEqual(paddedInput, paddedStored);
        clientId = trip["client_id"];
        if (isMatch) {
            success = true;
            tx.update(tripRef, {
                status: "COMPLETED",
                completed_at: admin.firestore.FieldValue.serverTimestamp(),
                delivery_code_verified_at: admin.firestore.FieldValue.serverTimestamp(),
            });
        }
        else {
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
                throw new https_1.HttpsError("failed-precondition", "DELIVERY_CODE_LOCKED");
            }
            const remaining = MAX_ATTEMPTS - newAttempts;
            throw new https_1.HttpsError("failed-precondition", `DELIVERY_CODE_INVALID:${remaining}`);
        }
    });
    if (success && clientId) {
        const clientSnap = await db.collection("users").doc(clientId).get();
        const fcmToken = clientSnap.data()?.["fcm_token"];
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
});
//# sourceMappingURL=completeTrip.js.map