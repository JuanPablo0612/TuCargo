package com.juanpablo0612.tucargo.data.auth

import com.juanpablo0612.tucargo.data.user.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(
        email: String,
        password: String,
        fullName: String,
        phone: String,
        role: String
    ): Result<User>
    suspend fun logout(): Result<Unit>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun getCurrentUser(): User?
    fun observeAuthState(): Flow<User?>
}
