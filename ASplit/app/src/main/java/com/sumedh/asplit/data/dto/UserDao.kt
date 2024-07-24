package com.sumedh.asplit.data.dto

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sumedh.asplit.domain.model.User


@Entity
data class UserDao(
    @PrimaryKey val name: String,
    val balance: Double = 0.0
)

fun UserDao.toUser(): User {
    return User(
        name = name,
        balance = balance
    )
}
