package io.github.dsyphr.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.messaging.FirebaseMessaging
import io.github.dsyphr.core.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthRepository @Inject constructor(
    private val auth: FirebaseAuth
) : AuthRepository {

    override val currentUser: MutableStateFlow<FirebaseUser?> = MutableStateFlow(auth.currentUser)

    private val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        currentUser.value = firebaseAuth.currentUser
    }

    init {
        auth.addAuthStateListener(authListener)
    }

    fun dispose() {
        auth.removeAuthStateListener(authListener)
    }

    override suspend fun signInWithEmailAndPassword(email: String, password: String): Result<FirebaseUser?> {
        return try {
            val result = auth.signInWithEmailAndPassword(email.trim(), password).await()
            Result.success(result.user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createUserWithEmailAndPassword(email: String, password: String): Result<FirebaseUser?> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
            Result.success(result.user)
        } catch (e: FirebaseAuthWeakPasswordException) {
            Result.failure(FirebaseAuthException("Password is too weak. Use at least 6 characters."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendEmailVerification(): Result<Unit> {
        return try {
            auth.currentUser?.sendEmailVerification()?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            FirebaseMessaging.getInstance().deleteToken().await()
            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun handleUserCollisionException(exception: FirebaseAuthUserCollisionException): Result<String> {
        return Result.failure(exception)
    }
}

class FirebaseAuthException(message: String) : Exception(message)
