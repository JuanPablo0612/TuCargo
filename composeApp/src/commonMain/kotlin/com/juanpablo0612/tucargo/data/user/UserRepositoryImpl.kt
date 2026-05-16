package com.juanpablo0612.tucargo.data.user

import com.juanpablo0612.tucargo.data.common.safeCall
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlin.time.Clock

class UserRepositoryImpl(
    private val auth: FirebaseAuth,
    firestore: FirebaseFirestore,
) : UserRepository {

    private val usersCollection = firestore.collection("users")

    override suspend fun updateDriverStatus(userId: String, isOnline: Boolean): Result<Unit> = safeCall {
        usersCollection.document(userId).update(
            mapOf(
                "is_online" to isOnline,
                "last_status_update" to Clock.System.now().toEpochMilliseconds()
            )
        )
    }

    override fun getCurrentUserId(): String? = auth.currentUser?.uid

    override fun isUserLoggedIn(): Boolean = auth.currentUser != null

    override suspend fun getCurrentUser(): Result<User> = safeCall {
        val uid = auth.currentUser?.uid ?: throw Exception("User not authenticated")
        val snapshot = usersCollection.document(uid).get()
        snapshot.data<User>()
    }

    override suspend fun createUser(user: User): Result<Unit> = safeCall {
        val uid = auth.currentUser?.uid ?: throw Exception("User not authenticated")
        usersCollection.document(uid).set(user.copy(id = uid))
    }

    override suspend fun updateUser(user: User): Result<Unit> = safeCall {
        val uid = auth.currentUser?.uid ?: throw Exception("User not authenticated")
        usersCollection.document(uid).set(user, merge = true)
    }

    override suspend fun signOut() = auth.signOut()
}
