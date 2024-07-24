package com.sumedh.asplit.data.repository

import com.sumedh.asplit.data.dto.ExpenseDao
import com.sumedh.asplit.data.local.ExpenseDao
import com.sumedh.asplit.domain.repository.ExpenseRepository

class ExpenseRepositoryImpl(
    private val expenseDao: ExpenseDao
) : ExpenseRepository {
    override suspend fun addExpense(expense: com.sumedh.asplit.data.dto.ExpenseDao) {
        expenseDao.insertExpense(expense)
    }

    override suspend fun getAllExpenses(): List<com.sumedh.asplit.data.dto.ExpenseDao> {
        return expenseDao.getAllExpenses()
    }

    override suspend fun clearExpenses() {
        expenseDao.clearExpenses()
    }

}