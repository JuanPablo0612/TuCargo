package com.juanpablo0612.tucargo.data.auth

import com.juanpablo0612.tucargo.data.common.safeCall
import com.juanpablo0612.tucargo.data.user.User
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.firestore.FirebaseFirestore

class AuthRepositoryImpl(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<User> = safeCall {
        val authResult = auth.signInWithEmailAndPassword(email, password)
        val uid = authResult.user?.uid ?: throw Exception("ID no encontrado")

        val userDoc = firestore.collection("users").document(uid).get()

        User(
            id = uid,
            email = email,
            role = userDoc.get<String>("role"),
            fullName = userDoc.get<String>("full_name")
        )
    }

    override suspend fun register(
        email: String,
        password: String,
        fullName: String
    ): Result<User> = safeCall {
        val authResult = auth.createUserWithEmailAndPassword(email, password)
        val uid = authResult.user?.uid ?: throw Exception("ID no encontrado")

        val user = User(
            id = uid,
            email = email,
            role = "CLIENT",
            fullName = fullName
        )

        firestore.collection("users").document(uid).set(user)
        user
    }
}
