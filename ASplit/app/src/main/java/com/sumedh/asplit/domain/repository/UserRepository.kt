package com.sumedh.asplit.domain.repository

interface UserRepository {
    suspend fun getAllUsers(): List<com.sumedh.asplit.data.dto.UserDao>
    suspend fun getUser(user: String): com.sumedh.asplit.data.dto.UserDao
    suspend fun updateUserBalance(name:String, balance:Double)

}