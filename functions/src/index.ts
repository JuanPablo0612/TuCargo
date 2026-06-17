import { setGlobalOptions } from "firebase-functions/options";
import * as admin from "firebase-admin";

admin.initializeApp();

setGlobalOptions({ maxInstances: 10 });

export { onUserDocumentCreated } from "./onUserDocumentCreated";
export { onTripCreate } from "./onTripCreate";
export { requestTrip } from "./requestTrip";
export { acceptOffer } from "./acceptOffer";
export { rejectOffer } from "./rejectOffer";
export { completeTrip } from "./completeTrip";
export { updateTripStatus } from "./updateTripStatus";
export { approveDriver } from "./approveDriver";
export { cleanStaleDriverLocations } from "./cleanStaleDriverLocations";
export { createQuote } from "./createQuote";
