package com.sumedh.asplit.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.sumedh.asplit.data.dto.ExpenseDao

@Dao
interface ExpenseDao {

    @Query("SELECT * FROM ExpenseDao")
    fun getAllExpenses(): List<ExpenseDao>

    @Insert
    fun insertExpense(expense: ExpenseDao)

    @Query("DELETE FROM ExpenseDao")
    fun clearExpenses()
}