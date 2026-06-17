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
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.createQuote = void 0;
const admin = __importStar(require("firebase-admin"));
const https_1 = require("firebase-functions/https");
const params_1 = require("firebase-functions/params");
const axios_1 = __importDefault(require("axios"));
const pricing_1 = require("./pricing");
const GOOGLE_MAPS_SERVER_KEY = (0, params_1.defineSecret)("GOOGLE_MAPS_SERVER_KEY");
async function fetchRouteWithRetry(apiKey, originLng, originLat, destLng, destLat) {
    const url = "https://routes.googleapis.com/directions/v2:computeRoutes";
    const body = {
        origin: { location: { latLng: { latitude: originLat, longitude: originLng } } },
        destination: { location: { latLng: { latitude: destLat, longitude: destLng } } },
        travelMode: "DRIVE",
        routingPreference: "TRAFFIC_AWARE",
    };
    const headers = {
        "Content-Type": "application/json",
        "X-Goog-Api-Key": apiKey,
        "X-Goog-FieldMask": "routes.distanceMeters,routes.polyline.encodedPolyline",
    };
    let lastError;
    const delays = [0, 200, 500];
    for (const delay of delays) {
        if (delay > 0)
            await new Promise((r) => setTimeout(r, delay));
        try {
            const response = await axios_1.default.post(url, body, { headers, timeout: 10000 });
            const routes = response.data?.routes;
            if (!routes || routes.length === 0) {
                throw new https_1.HttpsError("not-found", "NO_ROUTE");
            }
            const route = routes[0];
            return {
                distanceM: route.distanceMeters,
                polyline: route.polyline.encodedPolyline,
            };
        }
        catch (e) {
            if (e instanceof https_1.HttpsError)
                throw e;
            lastError = e;
        }
    }
    throw new https_1.HttpsError("unavailable", "SERVICE_UNAVAILABLE", lastError instanceof Error ? lastError.message : undefined);
}
exports.createQuote = (0, https_1.onCall)({ secrets: [GOOGLE_MAPS_SERVER_KEY] }, async (request) => {
    if (!request.auth || request.auth.token["role"] !== "CLIENT") {
        throw new https_1.HttpsError("permission-denied", "Clients only");
    }
    const { originLat, originLng, originAddr, destLat, destLng, destAddr, } = request.data;
    if (typeof originLat !== "number" ||
        typeof originLng !== "number" ||
        typeof destLat !== "number" ||
        typeof destLng !== "number" ||
        Math.abs(originLat) > 90 ||
        Math.abs(originLng) > 180 ||
        Math.abs(destLat) > 90 ||
        Math.abs(destLng) > 180) {
        throw new https_1.HttpsError("invalid-argument", "Invalid coordinates");
    }
    if (originLat === destLat && originLng === destLng) {
        throw new https_1.HttpsError("invalid-argument", "SAME_ORIGIN_DEST");
    }
    const { distanceM, polyline } = await fetchRouteWithRetry(GOOGLE_MAPS_SERVER_KEY.value(), originLng, originLat, destLng, destLat);
    const distanceKmRaw = distanceM / 1000;
    if (distanceKmRaw > 60) {
        throw new https_1.HttpsError("invalid-argument", "QUOTE_OUT_OF_RANGE");
    }
    const distanceKm = Math.round(distanceKmRaw * 10) / 10;
    const db = admin.firestore();
    const configSnap = await db.collection("config").doc("app_config").get();
    if (!configSnap.exists) {
        throw new https_1.HttpsError("internal", "Missing app_config");
    }
    const config = configSnap.data();
    const baseFare = config["base_fare"];
    const perKmFare = config["per_km_fare"];
    const commissionRate = config["commission_rate"];
    const { totalPrice, commissionFee } = (0, pricing_1.computePrice)(distanceKm, baseFare, perKmFare, commissionRate);
    const validUntil = Date.now() + 5 * 60 * 1000;
    const quoteRef = db.collection("quotes").doc();
    const quoteData = {
        id: quoteRef.id,
        client_id: request.auth.uid,
        origin_lat: originLat,
        origin_lng: originLng,
        origin_addr: originAddr,
        dest_lat: destLat,
        dest_lng: destLng,
        dest_addr: destAddr,
        distance_km: distanceKm,
        polyline,
        base_fare: baseFare,
        per_km_fare: perKmFare,
        total_price: totalPrice,
        commission_fee: commissionFee,
        valid_until: validUntil,
        consumed: false,
        created_at: admin.firestore.FieldValue.serverTimestamp(),
    };
    await quoteRef.set(quoteData);
    return {
        quoteId: quoteRef.id,
        distanceKm,
        polyline,
        totalPrice,
        commissionFee,
        validUntil,
        baseFare,
        perKmFare,
        originLat,
        originLng,
        originAddr,
        destLat,
        destLng,
        destAddr,
    };
});
//# sourceMappingURL=createQuote.js.map