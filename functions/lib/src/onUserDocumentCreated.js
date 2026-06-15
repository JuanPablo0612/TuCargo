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
exports.onUserDocumentCreated = void 0;
const firestore_1 = require("firebase-functions/v2/firestore");
const admin = __importStar(require("firebase-admin"));
exports.onUserDocumentCreated = (0, firestore_1.onDocumentCreated)("users/{userId}", async (event) => {
    const data = event.data?.data();
    if (!data)
        return;
    const userId = event.params.userId;
    const role = data["role"] ?? "CLIENT";
    // Set custom claim — setCustomUserClaims is idempotent (overwrites)
    await admin.auth().setCustomUserClaims(userId, { role });
    if (role !== "DRIVER")
        return;
    const db = admin.firestore();
    const walletRef = db.collection("wallets").doc(userId);
    const driverRef = db.collection("drivers").doc(userId);
    const [walletSnap, driverSnap] = await Promise.all([
        walletRef.get(),
        driverRef.get(),
    ]);
    const batch = db.batch();
    if (!walletSnap.exists) {
        batch.set(walletRef, {
            currentBalance: 0,
            createdAt: admin.firestore.FieldValue.serverTimestamp(),
        });
    }
    if (!driverSnap.exists) {
        batch.set(driverRef, {
            userId,
            documentStatus: "PENDING",
            availability: "OFFLINE",
            lastLocationLat: null,
            lastLocationLng: null,
            lastLocationAt: null,
            createdAt: admin.firestore.FieldValue.serverTimestamp(),
        });
    }
    await batch.commit();
});
//# sourceMappingURL=onUserDocumentCreated.js.map