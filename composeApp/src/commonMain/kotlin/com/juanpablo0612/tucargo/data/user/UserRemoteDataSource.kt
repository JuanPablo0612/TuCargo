package com.juanpablo0612.tucargo.data.user

import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserRemoteDataSource(private val firestore: FirebaseFirestore) {

    private val usersCollection get() = firestore.collection("users")

    suspend fun getUser(uid: String): UserDto =
        usersCollection.document(uid).get().data<UserDto>()

    suspend fun createUser(uid: String, user: UserDto) {
        usersCollection.document(uid).set(user)
    }

    suspend fun updateUser(uid: String, user: UserDto) {
        usersCollection.document(uid).set(user, merge = true)
    }

    // Intentionally a named, single-purpose setter: arbitrary field maps made
    // it possible to write privileged fields (role, is_verified,
    // wallet_balance) from the client.
    suspend fun updateOnlineStatus(uid: String, isOnline: Boolean, timestampMillis: Long) {
        usersCollection.document(uid).update(
            mapOf(
                "is_online" to isOnline,
                "last_status_update" to timestampMillis
            )
        )
    }

    fun observeUser(uid: String): Flow<UserDto?> =
        usersCollection.document(uid).snapshots.map { snap ->
            runCatching { snap.data<UserDto>() }.getOrNull()
        }
}
