package com.juanpablo0612.tucargo.data.config

import com.juanpablo0612.tucargo.core.coroutines.AppDispatchers
import com.juanpablo0612.tucargo.data.common.safeCall
import com.juanpablo0612.tucargo.domain.model.AppError
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.time.Clock

// config/system changes rarely (operator edits it from the console), but it was
// re-read on every quote and availability toggle. Serve a recent value from
// memory to cut those repeated reads.
private const val CACHE_TTL_MS = 10 * 60_000L

class ConfigRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val dispatchers: AppDispatchers
) : ConfigRepository {

    private val configDoc = firestore.collection("config").document("system")

    private val cacheMutex = Mutex()
    private var cachedConfig: SystemConfig? = null
    private var cachedAtMs = 0L

    // Fails loudly instead of falling back to hardcoded defaults: trips must
    // never be priced from values the operator didn't set. The config/system
    // seed document is described in the README.
    override suspend fun getSystemConfig(): Result<SystemConfig> = safeCall {
        cacheMutex.withLock {
            val now = Clock.System.now().toEpochMilliseconds()
            cachedConfig?.let { cached ->
                if (now - cachedAtMs < CACHE_TTL_MS) return@withLock cached
            }

            val config = withContext(dispatchers.io) {
                val snapshot = configDoc.get()
                if (!snapshot.exists) {
                    throw AppError.DataCorruption("The config/system document does not exist in Firestore")
                }
                snapshot.data<SystemConfig>()
            }
            cachedConfig = config
            cachedAtMs = now
            config
        }
    }
}
