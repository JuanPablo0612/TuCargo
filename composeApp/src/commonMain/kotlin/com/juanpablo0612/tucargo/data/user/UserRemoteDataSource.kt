package com.juanpablo0612.tucargo.data.user

import dev.gitlive.firebase.firestore.FirebaseFirestore

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

    suspend fun updateFields(uid: String, fields: Map<String, Any?>) {
        usersCollection.document(uid).update(fields)
    }
}
