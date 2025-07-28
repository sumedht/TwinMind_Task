package com.sumedh.twinmind.app.domain.usecase

import com.google.firebase.auth.AuthCredential
import com.sumedh.twinmind.app.data.model.User
import com.sumedh.twinmind.app.domain.repository.AuthRepository
import com.sumedh.twinmind.app.util.Result
import javax.inject.Inject


class SignInUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(credential: AuthCredential): Result<User> {
        return authRepository.signInWithGoogle(credential)
    }
}