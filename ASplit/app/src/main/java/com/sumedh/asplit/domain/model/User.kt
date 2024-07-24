package com.sumedh.asplit.domain.model

import androidx.room.PrimaryKey

data class User(
    val name: String,
    val balance: Double = 0.0
)
