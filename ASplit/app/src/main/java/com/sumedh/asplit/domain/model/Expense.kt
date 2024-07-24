package com.sumedh.asplit.domain.model

data class Expense(
    val id: Int = 0,
    val amount: Double,
    val payer: String,
    val participants: List<String>
)
