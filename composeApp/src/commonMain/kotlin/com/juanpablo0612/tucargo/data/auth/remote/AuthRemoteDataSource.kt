package com.juanpablo0612.tucargo.data.auth.remote

import dev.gitlive.firebase.auth.FirebaseAuth

class AuthRemoteDataSource(private val auth: FirebaseAuth) {
    suspend fun login(email: String, password: String) {
        
    }
}