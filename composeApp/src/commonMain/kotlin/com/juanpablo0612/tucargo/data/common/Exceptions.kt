package com.juanpablo0612.tucargo.data.common

import com.juanpablo0612.tucargo.domain.model.AppError
import dev.gitlive.firebase.FirebaseNetworkException
import dev.gitlive.firebase.auth.FirebaseAuthInvalidCredentialsException
import dev.gitlive.firebase.auth.FirebaseAuthUserCollisionException
import dev.gitlive.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.CancellationException

object ExceptionMapper {
    fun map(e: Throwable): AppError =
        when (e) {
            is AppError -> e
            is FirebaseAuthInvalidCredentialsException ->
                AppError.Auth.InvalidCredentials

            is FirebaseAuthUserCollisionException ->
                AppError.Auth.EmailAlreadyInUse

            is FirebaseAuthWeakPasswordException ->
                AppError.Auth.WeakPassword

            is FirebaseNetworkException ->
                AppError.Network

            else ->
                AppError.Unknown(e)
        }
}

suspend fun <T> safeCall(block: suspend () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: AppError) {
        Result.failure(e)
    } catch (e: Exception) {
        Result.failure(ExceptionMapper.map(e))
    }
