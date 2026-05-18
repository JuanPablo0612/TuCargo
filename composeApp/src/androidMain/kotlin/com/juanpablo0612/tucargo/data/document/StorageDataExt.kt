package com.juanpablo0612.tucargo.data.document

import dev.gitlive.firebase.storage.Data

actual fun ByteArray.toStorageData(): Data = Data(this)
