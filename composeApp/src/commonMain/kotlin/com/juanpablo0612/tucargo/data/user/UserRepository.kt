package com.juanpablo0612.tucargo.data.user

import com.juanpablo0612.tucargo.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun updateDriverStatus(userId: String, isOnline: Boolean): Result<Unit>
    fun getCurrentUserId(): String?
    fun isUserLoggedIn(): Boolean
    suspend fun getCurrentUser(): Result<User>
    suspend fun createUser(user: User): Result<Unit>
    suspend fun updateUser(user: User): Result<Unit>
    fun observeCurrentUser(): Flow<User?>

    // Admin-only (enforced by the Firestore rules, not by this layer).
    suspend fun getPendingDrivers(): Result<List<User>>
    suspend fun setDriverVerified(userId: String, verified: Boolean): Result<Unit>
}
