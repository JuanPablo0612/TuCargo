package com.juanpablo0612.tucargo.data.user

import com.juanpablo0612.tucargo.core.coroutines.AppDispatchers
import com.juanpablo0612.tucargo.data.common.safeCall
import com.juanpablo0612.tucargo.domain.model.AppError
import com.juanpablo0612.tucargo.domain.model.User
import dev.gitlive.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.time.Clock

class UserRepositoryImpl(
    private val auth: FirebaseAuth,
    private val userRemoteDataSource: UserRemoteDataSource,
    private val dispatchers: AppDispatchers
) : UserRepository {

    override suspend fun updateDriverStatus(userId: String, isOnline: Boolean): Result<Unit> = safeCall {
        withContext(dispatchers.io) {
            userRemoteDataSource.updateOnlineStatus(
                uid = userId,
                isOnline = isOnline,
                timestampMillis = Clock.System.now().toEpochMilliseconds()
            )
        }
    }

    override fun getCurrentUserId(): String? = auth.currentUser?.uid

    override fun isUserLoggedIn(): Boolean = auth.currentUser != null

    override suspend fun getCurrentUser(): Result<User> = safeCall {
        withContext(dispatchers.io) {
            val uid = auth.currentUser?.uid ?: throw AppError.Auth.NotAuthenticated
            userRemoteDataSource.getUser(uid).toDomain()
        }
    }

    override suspend fun createUser(user: User): Result<Unit> = safeCall {
        withContext(dispatchers.io) {
            val uid = auth.currentUser?.uid ?: throw AppError.Auth.NotAuthenticated
            userRemoteDataSource.createUser(uid, user.toDto().copy(id = uid))
        }
    }

    override suspend fun updateUser(user: User): Result<Unit> = safeCall {
        withContext(dispatchers.io) {
            val uid = auth.currentUser?.uid ?: throw AppError.Auth.NotAuthenticated
            userRemoteDataSource.updateUser(uid, user.toDto())
        }
    }

    override fun observeCurrentUser(): Flow<User?> {
        val uid = auth.currentUser?.uid ?: return flowOf(null)
        return userRemoteDataSource.observeUser(uid).map { it?.toDomain() }
    }
}
