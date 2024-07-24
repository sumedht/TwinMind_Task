package com.sumedh.asplit.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sumedh.asplit.data.dto.UserDao

@Dao
interface UserDao {

    @Query("SELECT * FROM UserDao")
    fun getAllUsers(): List<UserDao>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(user: UserDao)

    @Query("UPDATE UserDao SET balance = :balance WHERE name = :name")
    fun updateUserBalance(name: String, balance: Double)

    @Query("SELECT * FROM user WHERE name = :name")
    fun getUser(name: String): UserDao
}