package com.muscleplay.app.core.network.auth

actual fun createFirebaseAuthRepository(): FirebaseAuthRepository = IosFirebaseAuthRepository()

class IosFirebaseAuthRepository : FirebaseAuthRepository {
    override suspend fun signUpWithEmail(email: String, password: String): AuthResult<String> = AuthResult.Success("ios_uid_123")
    override suspend fun signInWithEmail(email: String, password: String): AuthResult<String> = AuthResult.Success("ios_uid_123")
    override suspend fun sendPasswordResetEmail(email: String): AuthResult<Unit> = AuthResult.Success(Unit)
    override suspend fun deleteAccount(): AuthResult<Unit> = AuthResult.Success(Unit)
    override suspend fun getIdToken(forceRefresh: Boolean): String? = "ios_id_token_123"
    override fun getCurrentUserUid(): String? = "ios_uid_123"
    override fun signOut() {}
}
