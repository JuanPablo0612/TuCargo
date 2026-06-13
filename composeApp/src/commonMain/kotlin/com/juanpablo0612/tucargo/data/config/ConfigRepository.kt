package com.juanpablo0612.tucargo.data.config

interface ConfigRepository {
    suspend fun getSystemConfig(): Result<SystemConfig>
}
