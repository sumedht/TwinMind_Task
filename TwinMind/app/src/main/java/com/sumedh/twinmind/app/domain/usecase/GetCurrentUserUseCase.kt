package com.sumedh.twinmind.app.domain.usecase

import com.sumedh.twinmind.app.data.model.User
import com.sumedh.twinmind.app.domain.repository.AuthRepository
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): User? {
        return authRepository.getCurrentUser()
    }
}