package com.juanpablo0612.tucargo.data.user

import com.juanpablo0612.tucargo.data.common.safeCall
import com.juanpablo0612.tucargo.domain.model.User
import dev.gitlive.firebase.auth.FirebaseAuth
import kotlin.time.Clock

class UserRepositoryImpl(
    private val auth: FirebaseAuth,
    private val userRemoteDataSource: UserRemoteDataSource
) : UserRepository {

    override suspend fun updateDriverStatus(userId: String, isOnline: Boolean): Result<Unit> = safeCall {
        userRemoteDataSource.updateFields(
            uid = userId,
            fields = mapOf(
                "is_online" to isOnline,
                "last_status_update" to Clock.System.now().toEpochMilliseconds()
            )
        )
    }

    override fun getCurrentUserId(): String? = auth.currentUser?.uid

    override fun isUserLoggedIn(): Boolean = auth.currentUser != null

    override suspend fun getCurrentUser(): Result<User> = safeCall {
        val uid = auth.currentUser?.uid ?: throw Exception("User not authenticated")
        userRemoteDataSource.getUser(uid).toDomain()
    }

    override suspend fun createUser(user: User): Result<Unit> = safeCall {
        val uid = auth.currentUser?.uid ?: throw Exception("User not authenticated")
        userRemoteDataSource.createUser(uid, user.toDto().copy(id = uid))
    }

    override suspend fun updateUser(user: User): Result<Unit> = safeCall {
        val uid = auth.currentUser?.uid ?: throw Exception("User not authenticated")
        userRemoteDataSource.updateUser(uid, user.toDto())
    }

    override suspend fun signOut() = auth.signOut()
}
