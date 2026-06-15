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
exports.updateTripStatus = void 0;
const admin = __importStar(require("firebase-admin"));
const https_1 = require("firebase-functions/v2/https");
const ACTION_MAP = {
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
exports.updateTripStatus = (0, https_1.onCall)(async (request) => {
    if (!request.auth || request.auth.token["role"] !== "DRIVER") {
        throw new https_1.HttpsError("permission-denied", "Drivers only");
    }
    const { tripId, action } = request.data;
    if (!tripId || !action) {
        throw new https_1.HttpsError("invalid-argument", "Missing tripId or action");
    }
    const transition = ACTION_MAP[action];
    if (!transition) {
        throw new https_1.HttpsError("invalid-argument", "Invalid action");
    }
    const db = admin.firestore();
    const uid = request.auth.uid;
    let clientId = "";
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
        if (trip["status"] !== transition.fromStatus) {
            throw new https_1.HttpsError("failed-precondition", "TRIP_INVALID_STATE");
        }
        clientId = trip["client_id"];
        tx.update(tripRef, {
            status: transition.toStatus,
            [transition.timestampField]: admin.firestore.FieldValue.serverTimestamp(),
        });
    });
    if (clientId) {
        const clientSnap = await db.collection("users").doc(clientId).get();
        const fcmToken = clientSnap.data()?.["fcm_token"];
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
});
//# sourceMappingURL=updateTripStatus.js.map