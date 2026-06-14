import * as admin from "firebase-admin";
import {
  onCall,
  HttpsError,
  CallableRequest,
} from "firebase-functions/v2/https";
import { defineSecret } from "firebase-functions/params";
import axios from "axios";
import { computePrice } from "./pricing";

const MAPBOX_SECRET_TOKEN = defineSecret("MAPBOX_SECRET_TOKEN");

interface CreateQuoteData {
  originLat: number;
  originLng: number;
  originAddr: string;
  destLat: number;
  destLng: number;
  destAddr: string;
}

async function fetchRouteWithRetry(
  token: string,
  originLng: number,
  originLat: number,
  destLng: number,
  destLat: number
): Promise<{ distanceM: number; polyline: string }> {
  const url =
    `https://api.mapbox.com/directions/v5/mapbox/driving/` +
    `${originLng},${originLat};${destLng},${destLat}` +
    `?geometries=polyline&overview=full&access_token=${token}`;

  let lastError: unknown;
  const delays = [0, 200, 500];
  for (const delay of delays) {
    if (delay > 0) await new Promise((r) => setTimeout(r, delay));
    try {
      const response = await axios.get(url, { timeout: 10000 });
      const routes = response.data?.routes;
      if (!routes || routes.length === 0) {
        throw new HttpsError("not-found", "NO_ROUTE");
      }
      const route = routes[0];
      return {
        distanceM: route.distance as number,
        polyline: route.geometry as string,
      };
    } catch (e) {
      if (e instanceof HttpsError) throw e;
      lastError = e;
    }
  }
  throw new HttpsError(
    "unavailable",
    "SERVICE_UNAVAILABLE",
    lastError instanceof Error ? lastError.message : undefined
  );
}

export const createQuote = onCall(
  { secrets: [MAPBOX_SECRET_TOKEN] },
  async (request: CallableRequest<CreateQuoteData>) => {
    if (!request.auth || request.auth.token["role"] !== "CLIENT") {
      throw new HttpsError("permission-denied", "Clients only");
    }

    const {
      originLat,
      originLng,
      originAddr,
      destLat,
      destLng,
      destAddr,
    } = request.data;

    if (
      typeof originLat !== "number" ||
      typeof originLng !== "number" ||
      typeof destLat !== "number" ||
      typeof destLng !== "number" ||
      Math.abs(originLat) > 90 ||
      Math.abs(originLng) > 180 ||
      Math.abs(destLat) > 90 ||
      Math.abs(destLng) > 180
    ) {
      throw new HttpsError("invalid-argument", "Invalid coordinates");
    }

    if (originLat === destLat && originLng === destLng) {
      throw new HttpsError("invalid-argument", "SAME_ORIGIN_DEST");
    }

    const { distanceM, polyline } = await fetchRouteWithRetry(
      MAPBOX_SECRET_TOKEN.value(),
      originLng,
      originLat,
      destLng,
      destLat
    );

    const distanceKmRaw = distanceM / 1000;
    if (distanceKmRaw > 60) {
      throw new HttpsError("invalid-argument", "QUOTE_OUT_OF_RANGE");
    }

    const distanceKm = Math.round(distanceKmRaw * 10) / 10;

    const db = admin.firestore();
    const configSnap = await db.collection("config").doc("app_config").get();
    if (!configSnap.exists) {
      throw new HttpsError("internal", "Missing app_config");
    }
    const config = configSnap.data()!;
    const baseFare: number = config["base_fare"] as number;
    const perKmFare: number = config["per_km_fare"] as number;
    const commissionRate: number = config["commission_rate"] as number;

    const { totalPrice, commissionFee } = computePrice(
      distanceKm,
      baseFare,
      perKmFare,
      commissionRate
    );

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
  }
);
