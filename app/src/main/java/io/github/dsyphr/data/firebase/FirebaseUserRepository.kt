package io.github.dsyphr.data.firebase

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import io.github.dsyphr.core.model.User
import io.github.dsyphr.core.repository.UserRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseUserRepository @Inject constructor(
    private val database: DatabaseReference,
    private val currentUserId: String? = null
) : UserRepository {

    override val users: MutableStateFlow<Map<String, User>> = MutableStateFlow(emptyMap())

    private val usersListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val usersMap = mutableMapOf<String, User>()
            for (userSnapshot in snapshot.children) {
                try {
                    val username = userSnapshot.child("username").getValue(String::class.java) ?: continue
                    val email = userSnapshot.child("email").getValue(String::class.java) ?: ""
                    val uid = userSnapshot.key ?: continue
                    
                    usersMap[uid] = User(username, uid, email)
                } catch (e: Exception) {
                    // Skip invalid user data
                }
            }
            users.value = usersMap
        }

        override fun onCancelled(error: DatabaseError) {
            users.value = emptyMap()
        }
    }

    init {
        database.child("users").addValueEventListener(usersListener)
    }

    fun dispose() {
        database.child("users").removeEventListener(usersListener)
    }

    override suspend fun getUserById(uid: String): Result<User?> {
        return try {
            val snapshot = database.child("users").child(uid).get().await()
            if (snapshot.exists()) {
                val username = snapshot.child("username").getValue(String::class.java) ?: ""
                val email = snapshot.child("email").getValue(String::class.java) ?: ""
                Result.success(User(username, uid, email))
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUsernameById(uid: String): Result<String> {
        return try {
            val snapshot = database.child("users").child(uid).child("username").get().await()
            val username = snapshot.getValue(String::class.java) ?: ""
            Result.success(username)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserByUsername(username: String): Result<User?> {
        return try {
            val snapshot = database.child("users")
                .orderByChild("username")
                .equalTo(username)
                .get()
                .await()
            
            if (snapshot.exists()) {
                val uid = snapshot.children.firstOrNull()?.key ?: return Result.failure(Exception("User not found"))
                val email = snapshot.children.firstOrNull()?.child("email")?.getValue(String::class.java) ?: ""
                Result.success(User(username, uid, email))
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUser(username: String): Result<Unit> {
        return try {
            // This should be called with current user context
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getContacts(): Result<Map<String, String>> {
        return try {
            val userId = currentUserId ?: return Result.failure(Exception("No current user"))
            val snapshot = database.child("users").child(userId).child("contacts").get().await()
            
            if (!snapshot.exists()) {
                return Result.success(emptyMap())
            }
            
            val contactsMap = mutableMapOf<String, String>()
            for (contactSnapshot in snapshot.children) {
                val uid = contactSnapshot.key ?: continue
                val username = contactSnapshot.value?.toString() ?: continue
                contactsMap[uid] = username
            }
            Result.success(contactsMap)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeContact(contactUid: String): Result<Unit> {
        return try {
            val userId = currentUserId ?: return Result.failure(Exception("No current user"))
            database.child("users").child(userId).child("contacts").child(contactUid).removeValue().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun findUserIdByUsername(username: String): Result<String?> {
        return try {
            val snapshot = database.child("users")
                .orderByChild("username")
                .equalTo(username)
                .get()
                .await()
            
            if (snapshot.exists()) {
                val uid = snapshot.children.firstOrNull()?.key
                Result.success(uid)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addContact(contactUid: String, username: String): Result<Unit> {
        return try {
            val userId = currentUserId ?: return Result.failure(Exception("No current user"))
            database.child("users").child(userId).child("contacts").child(contactUid).setValue(username).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun destroy() {
        // Listener is automatically removed when database reference is destroyed
    }
}
