import * as admin from "firebase-admin";
import {
  onCall,
  HttpsError,
  CallableRequest,
} from "firebase-functions/https";

interface ApproveDriverData {
  driverId: string;
  action: "APPROVE" | "REJECT";
  reason?: string;
}

export const approveDriver = onCall(
  async (request: CallableRequest<ApproveDriverData>) => {
    if (!request.auth || request.auth.token["role"] !== "ADMIN") {
      throw new HttpsError("permission-denied", "Admins only");
    }

    const { driverId, action, reason } = request.data;
    if (!driverId || !action) {
      throw new HttpsError("invalid-argument", "Missing driverId or action");
    }
    if (action === "REJECT" && !reason) {
      throw new HttpsError("invalid-argument", "Rejection requires a reason");
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
    } else {
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
    const fcmToken = userSnap.data()?.["fcm_token"] as string | undefined;
    if (fcmToken) {
      const body =
        action === "APPROVE" ?
          "¡Tu documentación fue aprobada! Ya puedes comenzar a trabajar." :
          `Tu documentación fue rechazada. Razón: ${reason}`;
      await admin
        .messaging()
        .send({ token: fcmToken, notification: { body } })
        .catch(() => undefined);
    }

    return { success: true };
  }
);
