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
exports.rejectOffer = void 0;
const admin = __importStar(require("firebase-admin"));
const https_1 = require("firebase-functions/v2/https");
exports.rejectOffer = (0, https_1.onCall)(async (request) => {
    if (!request.auth || request.auth.token["role"] !== "DRIVER") {
        throw new https_1.HttpsError("permission-denied", "Drivers only");
    }
    const { offerId } = request.data;
    if (!offerId) {
        throw new https_1.HttpsError("invalid-argument", "Missing offerId");
    }
    const db = admin.firestore();
    const uid = request.auth.uid;
    const offerRef = db.collection("trip_offers").doc(offerId);
    const offerSnap = await offerRef.get();
    if (!offerSnap.exists) {
        throw new https_1.HttpsError("not-found", "Offer not found");
    }
    const offer = offerSnap.data();
    if (offer["driver_id"] !== uid) {
        throw new https_1.HttpsError("permission-denied", "Not your offer");
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
});
//# sourceMappingURL=rejectOffer.js.map