package com.sumedh.twinmind.app.domain.repository

import com.google.firebase.auth.AuthCredential
import com.sumedh.twinmind.app.data.model.User
import com.sumedh.twinmind.app.util.Result

interface AuthRepository {
    fun getCurrentUser(): User?
    suspend fun signInWithGoogle(credential: AuthCredential): Result<User>
    fun signOut()
}