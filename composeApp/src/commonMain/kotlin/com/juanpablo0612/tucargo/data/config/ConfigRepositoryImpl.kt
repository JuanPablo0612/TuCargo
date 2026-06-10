package com.juanpablo0612.tucargo.data.config

import com.juanpablo0612.tucargo.core.coroutines.AppDispatchers
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ConfigRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val dispatchers: AppDispatchers
) : ConfigRepository {

    private val configDoc = firestore.collection("config").document("system")

    override suspend fun getSystemConfig(): SystemConfig = withContext(dispatchers.io) {
        try {
            configDoc.get().data<SystemConfig>()
        } catch (e: Exception) {
            SystemConfig()
        }
    }

    override fun observeSystemConfig(): Flow<SystemConfig> =
        configDoc.snapshots.map {
            try { it.data<SystemConfig>() } catch (e: Exception) { SystemConfig() }
        }
}
