package io.github.dsyphr.core.repository

import io.github.dsyphr.core.model.User
import kotlinx.coroutines.flow.StateFlow

interface UserRepository {
    val users: StateFlow<Map<String, User>>
    suspend fun getUserById(uid: String): Result<User?>
    suspend fun getUsernameById(uid: String): Result<String>
    suspend fun getUserByUsername(username: String): Result<User?>
    suspend fun updateUser(username: String): Result<Unit>
    suspend fun getContacts(): Result<Map<String, String>>
    suspend fun addContact(contactUid: String, username: String): Result<Unit>
    suspend fun removeContact(contactUid: String): Result<Unit>
    suspend fun findUserIdByUsername(username: String): Result<String?>
}
