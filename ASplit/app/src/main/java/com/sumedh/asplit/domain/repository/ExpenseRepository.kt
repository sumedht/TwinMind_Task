package com.sumedh.asplit.domain.repository

import com.sumedh.asplit.data.dto.ExpenseDao

interface ExpenseRepository {
    suspend fun addExpense(expense: ExpenseDao)
    suspend fun getAllExpenses() : List<ExpenseDao>
    suspend fun clearExpenses()
}