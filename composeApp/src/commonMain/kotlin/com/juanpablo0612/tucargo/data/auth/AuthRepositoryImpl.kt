package com.juanpablo0612.tucargo.data.auth

import com.juanpablo0612.tucargo.core.coroutines.AppDispatchers
import com.juanpablo0612.tucargo.core.logging.logError
import com.juanpablo0612.tucargo.data.common.safeCall
import com.juanpablo0612.tucargo.data.user.UserDto
import com.juanpablo0612.tucargo.data.user.UserRemoteDataSource
import com.juanpablo0612.tucargo.data.user.toDomain
import com.juanpablo0612.tucargo.domain.model.User
import com.juanpablo0612.tucargo.domain.model.UserRole
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class AuthRepositoryImpl(
    private val authDataSource: AuthRemoteDataSource,
    private val userDataSource: UserRemoteDataSource,
    private val dispatchers: AppDispatchers
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<User> =
        safeCall {
            withContext(dispatchers.io) {
                val uid = authDataSource.signIn(email, password)
                userDataSource.getUser(uid).toDomain()
            }
        }

    override suspend fun register(
        email: String,
        password: String,
        fullName: String,
        phone: String,
        role: UserRole
    ): Result<User> = safeCall {
        withContext(dispatchers.io) {
            val uid = authDataSource.createAccount(email, password)
            val userDto = UserDto(
                id = uid,
                email = email,
                role = role.name,
                fullName = fullName,
                phone = phone,
                status = "ACTIVE"
            )
            userDataSource.createUser(uid, userDto)
            userDto.toDomain()
        }
    }

    override suspend fun logout(): Result<Unit> =
        safeCall { 
            withContext(dispatchers.io) {
                authDataSource.signOut() 
            }
        }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> =
        safeCall { 
            withContext(dispatchers.io) {
                authDataSource.sendPasswordResetEmail(email) 
            }
        }

    override suspend fun getCurrentUser(): User? {
        val uid = authDataSource.getCurrentUserId() ?: return null
        return fetchUser(uid)
    }

    override fun observeAuthState(): Flow<User?> =
        authDataSource.observeAuthStateChanges().map { uid ->
            if (uid == null) null else fetchUser(uid)
        }

    // The Flow<User?> contract treats an unreadable profile as logged out;
    // retry and log first so the failure is at least visible.
    private suspend fun fetchUser(uid: String): User? {
        var lastError: Exception? = null
        repeat(USER_FETCH_ATTEMPTS) {
            try {
                return userDataSource.getUser(uid).toDomain()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                lastError = e
            }
        }
        logError(
            "AuthRepository",
            "Failed to load user $uid after $USER_FETCH_ATTEMPTS attempts: ${lastError?.message}"
        )
        return null
    }

    private companion object {
        const val USER_FETCH_ATTEMPTS = 2
    }
}
