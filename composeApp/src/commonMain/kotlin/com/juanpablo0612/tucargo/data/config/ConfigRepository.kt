package com.juanpablo0612.tucargo.data.config

import kotlinx.coroutines.flow.Flow

interface ConfigRepository {
    suspend fun getSystemConfig(): SystemConfig
    fun observeSystemConfig(): Flow<SystemConfig>
}
