package com.juanpablo0612.tucargo.core.logging

import android.util.Log

actual fun logError(tag: String, message: String) {
    Log.e(tag, message)
}
