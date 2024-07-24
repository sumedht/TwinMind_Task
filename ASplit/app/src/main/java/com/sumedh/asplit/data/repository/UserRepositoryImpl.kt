package com.sumedh.asplit.data.repository

import com.sumedh.asplit.data.dto.UserDao
import com.sumedh.asplit.data.local.UserDao
import com.sumedh.asplit.domain.repository.UserRepository

class UserRepositoryImpl(
    private val userDao: UserDao
) : UserRepository {
    override suspend fun getAllUsers(): List<com.sumedh.asplit.data.dto.UserDao> {
        return userDao.getAllUsers()
    }

    override suspend fun getUser(user: String): com.sumedh.asplit.data.dto.UserDao {
        return userDao.getUser(user)
    }

    override suspend fun updateUserBalance(name: String, balance: Double) {
        return userDao.updateUserBalance(name, balance)
    }
}