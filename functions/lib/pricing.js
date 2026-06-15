"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.roundToNearest100 = roundToNearest100;
exports.computePrice = computePrice;
function roundToNearest100(value) {
    return Math.round(value / 100) * 100;
}
function computePrice(distanceKm, baseFare, perKmFare, commissionRate) {
    const distanceCharged = Math.max(distanceKm, 1.0);
    const rawTotal = baseFare + (distanceCharged - 1) * perKmFare;
    const totalPrice = roundToNearest100(rawTotal);
    const rawCommission = Math.floor((totalPrice * commissionRate) / 100);
    const commissionFee = roundToNearest100(rawCommission);
    return { totalPrice, commissionFee };
}
//# sourceMappingURL=pricing.js.map