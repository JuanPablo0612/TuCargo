package com.juanpablo0612.tucargo.data.config

import com.juanpablo0612.tucargo.core.coroutines.AppDispatchers
import com.juanpablo0612.tucargo.data.common.safeCall
import com.juanpablo0612.tucargo.domain.model.AppError
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.withContext

class ConfigRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val dispatchers: AppDispatchers
) : ConfigRepository {

    private val configDoc = firestore.collection("config").document("system")

    // Fails loudly instead of falling back to hardcoded defaults: trips must
    // never be priced from values the operator didn't set. The config/system
    // seed document is described in the README.
    override suspend fun getSystemConfig(): Result<SystemConfig> = safeCall {
        withContext(dispatchers.io) {
            val snapshot = configDoc.get()
            if (!snapshot.exists) {
                throw AppError.DataCorruption("The config/system document does not exist in Firestore")
            }
            snapshot.data<SystemConfig>()
        }
    }
}
