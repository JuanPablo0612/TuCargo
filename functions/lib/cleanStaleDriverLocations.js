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
exports.cleanStaleDriverLocations = void 0;
const admin = __importStar(require("firebase-admin"));
const scheduler_1 = require("firebase-functions/scheduler");
exports.cleanStaleDriverLocations = (0, scheduler_1.onSchedule)("every 2 minutes", async () => {
    const db = admin.database();
    const cutoffMs = Date.now() - 90_000;
    const snap = await db
        .ref("driver_locations")
        .orderByChild("capturedAt")
        .endAt(cutoffMs)
        .get();
    if (!snap.exists())
        return;
    const deletions = {};
    snap.forEach((child) => {
        deletions[child.key] = null;
    });
    if (Object.keys(deletions).length > 0) {
        await db.ref("driver_locations").update(deletions);
    }
});
//# sourceMappingURL=cleanStaleDriverLocations.js.map