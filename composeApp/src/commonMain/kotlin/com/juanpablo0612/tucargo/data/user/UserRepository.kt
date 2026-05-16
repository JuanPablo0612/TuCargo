package com.juanpablo0612.tucargo.data.user

interface UserRepository {
    suspend fun updateDriverStatus(userId: String, isOnline: Boolean): Result<Unit>
    fun getCurrentUserId(): String?
    fun isUserLoggedIn(): Boolean
    suspend fun getCurrentUser(): Result<User>
    suspend fun createUser(user: User): Result<Unit>
    suspend fun updateUser(user: User): Result<Unit>
    suspend fun signOut()
}
