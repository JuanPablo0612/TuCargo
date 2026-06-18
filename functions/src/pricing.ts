export function roundToNearest100(value: number): number {
  return Math.round(value / 100) * 100;
}

// Mirrors CalculateTripPriceUseCase in the app. Inputs come from config/system
// (see README "Seed the pricing config"); commissionPercentage is a fraction
// (e.g. 0.15), not a whole-number percent.
export function computePrice(
  distanceKm: number,
  basePrice: number,
  baseKmIncluded: number,
  pricePerKm: number,
  commissionPercentage: number
): { totalPrice: number; commissionFee: number } {
  const distanceCharged = Math.max(distanceKm - baseKmIncluded, 0);
  const rawTotal = basePrice + distanceCharged * pricePerKm;
  const totalPrice = roundToNearest100(rawTotal);
  const commissionFee = roundToNearest100(totalPrice * commissionPercentage);
  return { totalPrice, commissionFee };
}
