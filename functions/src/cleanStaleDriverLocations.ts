import * as admin from "firebase-admin";
import { onSchedule } from "firebase-functions/v2/scheduler";

export const cleanStaleDriverLocations = onSchedule("every 2 minutes", async () => {
    const db = admin.database();
    const cutoffMs = Date.now() - 90_000;

    const snap = await db
        .ref("driver_locations")
        .orderByChild("capturedAt")
        .endAt(cutoffMs)
        .get();

    if (!snap.exists()) return;

    const deletions: Record<string, null> = {};
    snap.forEach((child) => {
        deletions[child.key!] = null;
    });

    if (Object.keys(deletions).length > 0) {
        await db.ref("driver_locations").update(deletions);
    }
});
