import * as admin from "firebase-admin";
import { onSchedule } from "firebase-functions/scheduler";

// The driver app now removes its own driver_locations node when going offline
// (see TrackingRepository.clearLocation), so this sweep is only a safety net for
// abrupt disconnects/crashes — a 15-minute cadence is plenty and ~7.5x cheaper.
export const cleanStaleDriverLocations = onSchedule("every 15 minutes", async () => {
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
