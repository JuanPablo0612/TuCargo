// RUTA: composeApp/src/commonMain/kotlin/com/juanpablo0612/tucargo/data/user/UserRepository.kt

package com.juanpablo0612.tucargo.data.user

import com.juanpablo0612.tucargo.data.common.safeCall
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.firestore.FirebaseFirestore

class UserRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private val usersCollection = firestore.collection("users")

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    suspend fun getCurrentUser(): Result<User> = safeCall {
        val uid = auth.currentUser?.uid ?: throw Exception("User not authenticated")
        val snapshot = usersCollection.document(uid).get()
        snapshot.data<User>()
    }

    suspend fun createUser(user: User): Result<Unit> = safeCall {
        val uid = auth.currentUser?.uid ?: throw Exception("User not authenticated")
        usersCollection.document(uid).set(user.copy(id = uid))
    }

    suspend fun updateUser(user: User): Result<Unit> = safeCall {
        val uid = auth.currentUser?.uid ?: throw Exception("User not authenticated")
        usersCollection.document(uid).set(user, merge = true)
    }

    suspend fun signOut() = auth.signOut()
}
