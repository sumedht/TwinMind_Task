package com.sumedh.asplit.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sumedh.asplit.data.dto.ExpenseDao
import com.sumedh.asplit.data.dto.UserDao

@Database(entities = [ExpenseDao::class, UserDao::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun userDao(): UserDao
}