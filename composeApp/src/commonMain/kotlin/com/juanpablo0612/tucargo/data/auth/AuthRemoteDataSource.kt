package com.juanpablo0612.tucargo.data.auth

import com.juanpablo0612.tucargo.data.user.User
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthRemoteDataSource(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    suspend fun signIn(email: String, password: String): User {
        val result = auth.signInWithEmailAndPassword(email, password)
        val uid = result.user?.uid ?: error("UID not found after sign in")
        return firestore.collection("users").document(uid).get().data<User>()
    }

    suspend fun createUser(
        email: String,
        password: String,
        fullName: String,
        phone: String,
        role: String
    ): User {
        val result = auth.createUserWithEmailAndPassword(email, password)
        val uid = result.user?.uid ?: error("UID not found after registration")

        val user = User(
            id = uid,
            email = email,
            role = role,
            fullName = fullName,
            phone = phone,
            status = "ACTIVE"
        )
        firestore.collection("users").document(uid).set(user)
        return user
    }

    suspend fun signOut() {
        auth.signOut()
    }

    suspend fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
    }

    suspend fun getCurrentUser(): User? {
        val uid = auth.currentUser?.uid ?: return null
        return runCatching {
            firestore.collection("users").document(uid).get().data<User>()
        }.getOrNull()
    }

    fun observeAuthState(): Flow<User?> =
        auth.authStateChanged.map { firebaseUser ->
            if (firebaseUser == null) null
            else runCatching {
                firestore.collection("users").document(firebaseUser.uid).get().data<User>()
            }.getOrNull()
        }
}
