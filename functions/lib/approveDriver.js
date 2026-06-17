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
exports.approveDriver = void 0;
const admin = __importStar(require("firebase-admin"));
const https_1 = require("firebase-functions/https");
exports.approveDriver = (0, https_1.onCall)(async (request) => {
    if (!request.auth || request.auth.token["role"] !== "ADMIN") {
        throw new https_1.HttpsError("permission-denied", "Admins only");
    }
    const { driverId, action, reason } = request.data;
    if (!driverId || !action) {
        throw new https_1.HttpsError("invalid-argument", "Missing driverId or action");
    }
    if (action === "REJECT" && !reason) {
        throw new https_1.HttpsError("invalid-argument", "Rejection requires a reason");
    }
    const db = admin.firestore();
    const driverRef = db.collection("drivers").doc(driverId);
    const auditRef = db.collection("audit_log").doc();
    const now = admin.firestore.FieldValue.serverTimestamp();
    const batch = db.batch();
    if (action === "APPROVE") {
        batch.update(driverRef, {
            documentStatus: "APPROVED",
            approvedAt: now,
            approvedBy: request.auth.uid,
            updatedAt: now,
        });
        batch.set(auditRef, {
            action: "APPROVE_DRIVER",
            entityType: "driver",
            entityId: driverId,
            actorId: request.auth.uid,
            createdAt: now,
        });
    }
    else {
        batch.update(driverRef, {
            documentStatus: "REJECTED",
            rejectionReason: reason,
            updatedAt: now,
        });
        batch.set(auditRef, {
            action: "REJECT_DRIVER",
            entityType: "driver",
            entityId: driverId,
            reason,
            actorId: request.auth.uid,
            createdAt: now,
        });
    }
    await batch.commit();
    // FCM notification to driver
    const userSnap = await db.collection("users").doc(driverId).get();
    const fcmToken = userSnap.data()?.["fcm_token"];
    if (fcmToken) {
        const body = action === "APPROVE" ?
            "¡Tu documentación fue aprobada! Ya puedes comenzar a trabajar." :
            `Tu documentación fue rechazada. Razón: ${reason}`;
        await admin
            .messaging()
            .send({ token: fcmToken, notification: { body } })
            .catch(() => undefined);
    }
    return { success: true };
});
//# sourceMappingURL=approveDriver.js.map