package com.juanpablo0612.tucargo.data.auth

import com.juanpablo0612.tucargo.data.common.safeCall
import dev.gitlive.firebase.auth.FirebaseAuth

class AuthRepository(private val auth: FirebaseAuth) {
    suspend fun login(email: String, password: String): Result<Unit> =
        safeCall {
            auth.signInWithEmailAndPassword(email, password)
        }

    suspend fun register(email: String, password: String): Result<Unit> =
        safeCall {
            auth.createUserWithEmailAndPassword(email, password)
        }
}
