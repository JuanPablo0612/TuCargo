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
exports.approveDriver = exports.onUserDocumentCreated = exports.cleanStaleDriverLocations = exports.completeTrip = exports.updateTripStatus = exports.rejectOffer = exports.acceptOffer = exports.onTripCreate = exports.requestTrip = exports.createQuote = void 0;
const admin = __importStar(require("firebase-admin"));
admin.initializeApp();
var createQuote_1 = require("./createQuote");
Object.defineProperty(exports, "createQuote", { enumerable: true, get: function () { return createQuote_1.createQuote; } });
var requestTrip_1 = require("./requestTrip");
Object.defineProperty(exports, "requestTrip", { enumerable: true, get: function () { return requestTrip_1.requestTrip; } });
var onTripCreate_1 = require("./onTripCreate");
Object.defineProperty(exports, "onTripCreate", { enumerable: true, get: function () { return onTripCreate_1.onTripCreate; } });
var acceptOffer_1 = require("./acceptOffer");
Object.defineProperty(exports, "acceptOffer", { enumerable: true, get: function () { return acceptOffer_1.acceptOffer; } });
var rejectOffer_1 = require("./rejectOffer");
Object.defineProperty(exports, "rejectOffer", { enumerable: true, get: function () { return rejectOffer_1.rejectOffer; } });
var updateTripStatus_1 = require("./updateTripStatus");
Object.defineProperty(exports, "updateTripStatus", { enumerable: true, get: function () { return updateTripStatus_1.updateTripStatus; } });
var completeTrip_1 = require("./completeTrip");
Object.defineProperty(exports, "completeTrip", { enumerable: true, get: function () { return completeTrip_1.completeTrip; } });
var cleanStaleDriverLocations_1 = require("./cleanStaleDriverLocations");
Object.defineProperty(exports, "cleanStaleDriverLocations", { enumerable: true, get: function () { return cleanStaleDriverLocations_1.cleanStaleDriverLocations; } });
var onUserDocumentCreated_1 = require("./onUserDocumentCreated");
Object.defineProperty(exports, "onUserDocumentCreated", { enumerable: true, get: function () { return onUserDocumentCreated_1.onUserDocumentCreated; } });
var approveDriver_1 = require("./approveDriver");
Object.defineProperty(exports, "approveDriver", { enumerable: true, get: function () { return approveDriver_1.approveDriver; } });
//# sourceMappingURL=index.js.map