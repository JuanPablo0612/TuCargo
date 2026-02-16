package com.juanpablo0612.tucargo.data.common

import dev.gitlive.firebase.FirebaseNetworkException
import dev.gitlive.firebase.auth.FirebaseAuthInvalidCredentialsException
import dev.gitlive.firebase.auth.FirebaseAuthUserCollisionException

sealed class DataException(message: String?) : Exception(message) {
    class InvalidCredentials(message: String?) : DataException(message)
    class UserAlreadyExists(message: String?) : DataException(message)
    class Network(message: String?) : DataException(message)
    class Unknown(message: String?) : DataException(message)
}

object ExceptionMapper {
    fun map(e: Exception): DataException =
        when (e) {
            is FirebaseAuthInvalidCredentialsException ->
                DataException.InvalidCredentials(e.message)

            is FirebaseAuthUserCollisionException ->
                DataException.UserAlreadyExists(e.message)

            is FirebaseNetworkException ->
                DataException.Network(e.message)

            else ->
                DataException.Unknown(e.message)
        }
}

suspend fun <T> safeCall(
    block: suspend () -> T
): Result<T> {
    return try {
        Result.success(block())
    } catch (e: Exception) {
        Result.failure(ExceptionMapper.map(e))
    }
}