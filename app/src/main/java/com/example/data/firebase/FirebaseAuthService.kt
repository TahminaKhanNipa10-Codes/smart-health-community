package com.example.data.firebase

import android.util.Log
import com.example.data.model.UserEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class FirebaseAuthService {
    private val auth: FirebaseAuth by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.e("FirebaseAuthService", "FirebaseAuth init fallback: ${e.message}")
            FirebaseAuth.getInstance()
        }
    }

    val currentFirebaseUser: FirebaseUser?
        get() = try {
            auth.currentUser
        } catch (e: Exception) {
            null
        }

    val currentUserId: String?
        get() = currentFirebaseUser?.uid

    suspend fun registerUser(email: String, password: String): Result<FirebaseUser> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email.trim(), password).await()
            val user = authResult.user
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Registration failed: Firebase user is null"))
            }
        } catch (e: Exception) {
            Log.e("FirebaseAuthService", "Register error: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun loginUser(email: String, password: String): Result<FirebaseUser> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email.trim(), password).await()
            val user = authResult.user
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Login failed: Firebase user is null"))
            }
        } catch (e: Exception) {
            Log.e("FirebaseAuthService", "Login error: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogleCredential(idToken: String): Result<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val user = authResult.user
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Google sign in failed: Firebase user is null"))
            }
        } catch (e: Exception) {
            Log.e("FirebaseAuthService", "Google sign in error: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email.trim()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseAuthService", "Password reset error: ${e.message}", e)
            Result.failure(e)
        }
    }

    fun logout() {
        try {
            auth.signOut()
        } catch (e: Exception) {
            Log.e("FirebaseAuthService", "Logout error: ${e.message}", e)
        }
    }
}
