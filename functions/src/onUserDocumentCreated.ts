import { onDocumentCreated } from "firebase-functions/firestore";
import * as admin from "firebase-admin";

export const onUserDocumentCreated = onDocumentCreated(
  "users/{userId}",
  async (event) => {
    const data = event.data?.data();
    if (!data) return;

    const userId = event.params.userId;
    const role: string = (data["role"] as string) ?? "CLIENT";

    // Set custom claim — setCustomUserClaims is idempotent (overwrites)
    await admin.auth().setCustomUserClaims(userId, { role });

    if (role !== "DRIVER") return;

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
  }
);
