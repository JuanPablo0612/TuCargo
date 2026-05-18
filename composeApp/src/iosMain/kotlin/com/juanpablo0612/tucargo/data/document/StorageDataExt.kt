package com.juanpablo0612.tucargo.data.document

import dev.gitlive.firebase.storage.Data
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData

@OptIn(ExperimentalForeignApi::class)
actual fun ByteArray.toStorageData(): Data {
    val nsData = usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = size.toULong())
    }
    return Data(nsData)
}
