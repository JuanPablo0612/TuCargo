export function roundToNearest100(value: number): number {
  return Math.round(value / 100) * 100;
}

export function computePrice(
  distanceKm: number,
  baseFare: number,
  perKmFare: number,
  commissionRate: number
): { totalPrice: number; commissionFee: number } {
  const distanceCharged = Math.max(distanceKm, 1.0);
  const rawTotal = baseFare + (distanceCharged - 1) * perKmFare;
  const totalPrice = roundToNearest100(rawTotal);
  const rawCommission = Math.floor((totalPrice * commissionRate) / 100);
  const commissionFee = roundToNearest100(rawCommission);
  return { totalPrice, commissionFee };
}
