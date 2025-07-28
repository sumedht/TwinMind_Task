package com.sumedh.twinmind.app.data.repository

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.sumedh.twinmind.app.data.model.User
import com.sumedh.twinmind.app.domain.repository.AuthRepository
import kotlinx.coroutines.tasks.await
import com.sumedh.twinmind.app.util.Result
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth?
) : AuthRepository {

    override fun getCurrentUser(): User? {
        return firebaseAuth?.currentUser?.toUser()
    }

    override suspend fun signInWithGoogle(credential: AuthCredential): Result<User> {
        return try {
            val authResult = firebaseAuth?.signInWithCredential(credential)?.await()
            val user = authResult?.user?.toUser()
            if (user != null) {
                Result.Success(user)
            } else {
                Result.Error(Exception("Firebase authentication failed: user is null."))
            }
        } catch (e: Exception) {
            // Catch specific Firebase exceptions if needed for more granular error handling
            Result.Error(e)
        }
    }

    override fun signOut() {
        firebaseAuth?.signOut()
    }

    /**
     * Extension function to map a FirebaseUser to our domain User model.
     */
    private fun FirebaseUser.toUser(): User {
        return User(
            uid = this.uid,
            displayName = this.displayName,
            email = this.email
        )
    }
}