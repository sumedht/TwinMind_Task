package com.sumedh.asplit.domain.use_case

import com.sumedh.asplit.common.Resource
import com.sumedh.asplit.data.dto.toUser

import com.sumedh.asplit.domain.model.User
import com.sumedh.asplit.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetBalancesUseCase(
    private val userRepository: UserRepository
) {
    suspend fun invoke(): Flow<Resource<List<User>>> = flow {
        emit(Resource.Loading())
        val users = userRepository.getAllUsers().map { it.toUser() }
        emit(Resource.Success(users))
    }
}