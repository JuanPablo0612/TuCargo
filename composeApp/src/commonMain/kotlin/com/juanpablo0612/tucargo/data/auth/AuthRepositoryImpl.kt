package com.juanpablo0612.tucargo.data.auth

import com.juanpablo0612.tucargo.data.user.User
import com.juanpablo0612.tucargo.domain.model.AuthError
import kotlinx.coroutines.flow.Flow

class AuthRepositoryImpl(
    private val dataSource: AuthRemoteDataSource
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<User> =
        runCatching { dataSource.signIn(email, password) }.mapFailure()

    override suspend fun register(
        email: String,
        password: String,
        fullName: String,
        phone: String,
        role: String
    ): Result<User> =
        runCatching { dataSource.createUser(email, password, fullName, phone, role) }.mapFailure()

    override suspend fun logout(): Result<Unit> =
        runCatching { dataSource.signOut() }.mapFailure()

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> =
        runCatching { dataSource.sendPasswordResetEmail(email) }.mapFailure()

    override suspend fun getCurrentUser(): User? = dataSource.getCurrentUser()

    override fun observeAuthState(): Flow<User?> = dataSource.observeAuthState()

    private fun <T> Result<T>.mapFailure(): Result<T> =
        exceptionOrNull()?.let { Result.failure(mapException(it)) } ?: this

    private fun mapException(e: Throwable): AuthError {
        val msg = e.message?.uppercase() ?: ""
        return when {
            "INVALID_LOGIN_CREDENTIALS" in msg || "WRONG_PASSWORD" in msg ||
                    "INVALID_CREDENTIAL" in msg -> AuthError.InvalidCredentials
            "EMAIL_EXISTS" in msg || "EMAIL_ALREADY_IN_USE" in msg -> AuthError.EmailAlreadyInUse
            "WEAK_PASSWORD" in msg -> AuthError.WeakPassword
            "NETWORK" in msg -> AuthError.NetworkError
            else -> AuthError.Unknown(e.message)
        }
    }
}
