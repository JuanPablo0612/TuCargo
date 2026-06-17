"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.onTripCreate = void 0;
const firestore_1 = require("firebase-functions/firestore");
const dispatchTrip_1 = require("./dispatchTrip");
exports.onTripCreate = (0, firestore_1.onDocumentCreated)("trips/{tripId}", async (event) => {
    const data = event.data?.data();
    if (!data || data["status"] !== "REQUESTED")
        return;
    const tripId = event.params.tripId;
    await (0, dispatchTrip_1.dispatchTrip)(tripId);
});
//# sourceMappingURL=onTripCreate.js.map