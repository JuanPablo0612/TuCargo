import { onDocumentCreated } from "firebase-functions/firestore";
import { dispatchTrip } from "./dispatchTrip";

export const onTripCreate = onDocumentCreated(
  "trips/{tripId}",
  async (event) => {
    const data = event.data?.data();
    if (!data || data["status"] !== "REQUESTED") return;

    const tripId = event.params.tripId;
    await dispatchTrip(tripId);
  }
);
