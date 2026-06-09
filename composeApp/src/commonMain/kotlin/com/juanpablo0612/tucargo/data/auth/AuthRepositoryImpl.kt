package com.juanpablo0612.tucargo.data.auth

import com.juanpablo0612.tucargo.data.user.UserDto
import com.juanpablo0612.tucargo.data.user.UserRemoteDataSource
import com.juanpablo0612.tucargo.data.user.toDomain
import com.juanpablo0612.tucargo.domain.model.AuthError
import com.juanpablo0612.tucargo.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthRepositoryImpl(
    private val authDataSource: AuthRemoteDataSource,
    private val userDataSource: UserRemoteDataSource
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<User> =
        runCatching {
            val uid = authDataSource.signIn(email, password)
            userDataSource.getUser(uid).toDomain()
        }.mapFailure()

    override suspend fun register(
        email: String,
        password: String,
        fullName: String,
        phone: String,
        role: String
    ): Result<User> = runCatching {
        val uid = authDataSource.createAccount(email, password)
        val userDto = UserDto(
            id = uid,
            email = email,
            role = role,
            fullName = fullName,
            phone = phone,
            status = "ACTIVE"
        )
        userDataSource.createUser(uid, userDto)
        userDto.toDomain()
    }.mapFailure()

    override suspend fun logout(): Result<Unit> =
        runCatching { authDataSource.signOut() }.mapFailure()

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> =
        runCatching { authDataSource.sendPasswordResetEmail(email) }.mapFailure()

    override suspend fun getCurrentUser(): User? {
        val uid = authDataSource.getCurrentUserId() ?: return null
        return runCatching { userDataSource.getUser(uid).toDomain() }.getOrNull()
    }

    override fun observeAuthState(): Flow<User?> =
        authDataSource.observeAuthStateChanges().map { uid ->
            if (uid == null) null
            else runCatching { userDataSource.getUser(uid).toDomain() }.getOrNull()
        }

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
