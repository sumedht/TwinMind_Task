package com.sumedh.asplit.domain.model

data class SettlementTransaction(
    val payer: String,
    val receiver: String,
    val amount: Double
)

