package com.muscleplay.app.core.network.auth

sealed class AuthResult<out T> {
    data class Success<out T>(val data: T) : AuthResult<T>()
    data class Error(val message: String) : AuthResult<Nothing>()
}

interface FirebaseAuthRepository {
    suspend fun signUpWithEmail(email: String, password: String): AuthResult<String>
    suspend fun signInWithEmail(email: String, password: String): AuthResult<String>
    suspend fun sendPasswordResetEmail(email: String): AuthResult<Unit>
    suspend fun deleteAccount(): AuthResult<Unit>
    suspend fun getIdToken(forceRefresh: Boolean = false): String?
    fun getCurrentUserUid(): String?
    fun signOut()
}

expect fun createFirebaseAuthRepository(): FirebaseAuthRepository
