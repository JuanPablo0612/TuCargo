package com.juanpablo0612.tucargo.features.admin.home

import androidx.compose.runtime.Immutable
import com.juanpablo0612.tucargo.domain.model.User
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class AdminHomeState(
    val isLoading: Boolean = true,
    val pendingDrivers: ImmutableList<User> = persistentListOf(),
    val error: AdminHomeError? = null
)
