package com.juanpablo0612.tucargo.data.wallet

import com.juanpablo0612.tucargo.data.wallet.dto.*
import com.juanpablo0612.tucargo.domain.model.*

fun WalletBalanceDto.toDomain(): WalletBalance = WalletBalance(
    commissionOwed = Cop(commissionOwed),
    ivaOwed = Cop(ivaOwed),
    ssOwed = Cop(ssOwed),
    totalOwed = Cop(totalOwed)
)

fun DriverTaxProfileDto.toDomain(): DriverTaxProfile = DriverTaxProfile(
    documentType = documentType,
    documentNumber = documentNumber,
    billingEmail = billingEmail,
    residenceMunicipalityId = residenceMunicipalityId,
    ciiuCode = ciiuCode
)

fun CommissionTransactionDto.toDomain(id: String): CommissionTransaction = CommissionTransaction(
    id = id,
    type = try {
        CommissionTransactionType.valueOf(type)
    } catch (e: Exception) {
        CommissionTransactionType.COMMISSION_ACCRUAL
    },
    tripId = tripId,
    amount = Cop(amountCop),
    timestamp = 0L, // TODO: Implement ISO 8601 parsing if needed, or switch to Long in Firestore
    description = description
)

fun SettlementDto.toDomain(id: String): Settlement = Settlement(
    id = id,
    driverId = driverId,
    amountPaid = Cop(amountPaid),
    gatewayReference = gatewayReference,
    status = try {
        SettlementStatus.valueOf(status)
    } catch (e: Exception) {
        SettlementStatus.PENDING
    },
    invoiceId = invoiceId,
    timestamp = 0L // TODO: Implement ISO 8601 parsing
)

fun WalletConfigDto.toDomain(): WalletConfig = WalletConfig(
    maxCommissionDebt = Cop(maxCommissionDebt),
    warningThreshold1 = Cop(warningThreshold1),
    warningThreshold2 = Cop(warningThreshold2),
    settlementCadenceDays = settlementCadenceDays,
    ivaRate = ivaRate,
    ssDriverShare = ssDriverShare
)
