package com.juanpablo0612.tucargo.data.auth

import com.juanpablo0612.tucargo.data.user.User

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(email: String, password: String, fullName: String): Result<User>
}