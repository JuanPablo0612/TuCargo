package com.juanpablo0612.tucargo.domain.usecase

import com.juanpablo0612.tucargo.data.user.UserRepository
import com.juanpablo0612.tucargo.domain.model.User
import kotlinx.coroutines.flow.Flow

class ObserveCurrentUserUseCase(private val userRepository: UserRepository) {
    operator fun invoke(): Flow<User?> = userRepository.observeCurrentUser()
}
