package com.juanpablo0612.tucargo.data.auth

import dev.gitlive.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthRemoteDataSource(private val auth: FirebaseAuth) {

    suspend fun signIn(email: String, password: String): String {
        val result = auth.signInWithEmailAndPassword(email, password)
        return result.user?.uid ?: error("UID not found after sign in")
    }

    suspend fun createAccount(email: String, password: String): String {
        val result = auth.createUserWithEmailAndPassword(email, password)
        return result.user?.uid ?: error("UID not found after registration")
    }

    suspend fun signOut() {
        auth.signOut()
    }

    suspend fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
    }

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    fun observeAuthStateChanges(): Flow<String?> =
        auth.authStateChanged.map { it?.uid }
}
