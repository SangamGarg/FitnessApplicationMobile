package com.muscleplay.app.core.network.auth

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

actual fun createFirebaseAuthRepository(): FirebaseAuthRepository = AndroidFirebaseAuthRepository()

class AndroidFirebaseAuthRepository : FirebaseAuthRepository {
    private val firebaseAuth: FirebaseAuth
        get() = FirebaseAuth.getInstance()

    override suspend fun signUpWithEmail(email: String, password: String): AuthResult<String> =
        suspendCancellableCoroutine { continuation ->
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val uid = result.user?.uid ?: ""
                    if (continuation.isActive) continuation.resume(AuthResult.Success(uid))
                }
                .addOnFailureListener { exception ->
                    if (continuation.isActive) continuation.resume(AuthResult.Error(exception.message ?: "Registration failed"))
                }
        }

    override suspend fun signInWithEmail(email: String, password: String): AuthResult<String> =
        suspendCancellableCoroutine { continuation ->
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val uid = result.user?.uid ?: ""
                    if (continuation.isActive) continuation.resume(AuthResult.Success(uid))
                }
                .addOnFailureListener { exception ->
                    if (continuation.isActive) continuation.resume(AuthResult.Error(exception.message ?: "Authentication failed"))
                }
        }

    override suspend fun sendPasswordResetEmail(email: String): AuthResult<Unit> =
        suspendCancellableCoroutine { continuation ->
            firebaseAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    if (continuation.isActive) continuation.resume(AuthResult.Success(Unit))
                }
                .addOnFailureListener { exception ->
                    if (continuation.isActive) continuation.resume(AuthResult.Error(exception.message ?: "Password reset email failed"))
                }
        }

    override suspend fun deleteAccount(): AuthResult<Unit> =
        suspendCancellableCoroutine { continuation ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                user.delete()
                    .addOnSuccessListener {
                        if (continuation.isActive) continuation.resume(AuthResult.Success(Unit))
                    }
                    .addOnFailureListener { exception ->
                        if (continuation.isActive) continuation.resume(AuthResult.Error(exception.message ?: "Account deletion failed"))
                    }
            } else {
                if (continuation.isActive) continuation.resume(AuthResult.Success(Unit))
            }
        }

    override suspend fun getIdToken(forceRefresh: Boolean): String? =
        suspendCancellableCoroutine { continuation ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                user.getIdToken(forceRefresh)
                    .addOnSuccessListener { result ->
                        if (continuation.isActive) continuation.resume(result.token)
                    }
                    .addOnFailureListener {
                        if (continuation.isActive) continuation.resume(null)
                    }
            } else {
                if (continuation.isActive) continuation.resume(null)
            }
        }

    override fun getCurrentUserUid(): String? = firebaseAuth.currentUser?.uid

    override fun signOut() {
        firebaseAuth.signOut()
    }
}
