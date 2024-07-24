package com.sumedh.asplit.data.dto

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sumedh.asplit.domain.model.Expense

@Entity
data class ExpenseDao(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val payer: String,
    val participants: List<String>
)

fun ExpenseDao.toExpense(): Expense {
    return Expense(
        id = id,
        amount = amount,
        payer = payer,
        participants = participants
    )
}
