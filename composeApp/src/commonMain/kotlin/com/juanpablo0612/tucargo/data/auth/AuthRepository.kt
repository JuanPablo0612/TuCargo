// RUTA: composeApp/src/commonMain/kotlin/com/juanpablo0612/tucargo/data/auth/AuthRepository.kt

package com.juanpablo0612.tucargo.data.auth

import com.juanpablo0612.tucargo.data.common.safeCall
import com.juanpablo0612.tucargo.data.user.User
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.firestore.FirebaseFirestore

class AuthRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    suspend fun login(email: String, password: String): Result<User> = safeCall {
        val authResult = auth.signInWithEmailAndPassword(email, password)
        val uid = authResult.user?.uid ?: throw Exception("ID no encontrado")

        // Para Firebase Gitlive en KMP, se accede así:
        val userDoc = firestore.collection("users").document(uid).get()

        User(
            id = uid,
            email = email,
            role = userDoc.get<String>("role"),      // Especificamos el tipo <String>
            fullName = userDoc.get<String>("full_name") // Especificamos el tipo <String>
        )
    }

    suspend fun register(email: String, password: String, fullName: String): Result<User> = safeCall {
        val authResult = auth.createUserWithEmailAndPassword(email, password)
        val uid = authResult.user?.uid ?: throw Exception("ID no encontrado")

        val user = User(
            id = uid,
            email = email,
            role = "CLIENT", // Por defecto
            fullName = fullName
        )

        firestore.collection("users").document(uid).set(user)
        user
    }
}