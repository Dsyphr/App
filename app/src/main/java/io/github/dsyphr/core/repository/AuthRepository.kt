package io.github.dsyphr.core.repository

import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val currentUser: StateFlow<FirebaseUser?>
    suspend fun signInWithEmailAndPassword(email: String, password: String): Result<FirebaseUser?>
    suspend fun createUserWithEmailAndPassword(email: String, password: String): Result<FirebaseUser?>
    suspend fun sendEmailVerification(): Result<Unit>
    suspend fun signOut(): Result<Unit>
    
    fun handleUserCollisionException(exception: FirebaseAuthUserCollisionException): Result<String>
}
