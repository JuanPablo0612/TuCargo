package com.juanpablo0612.tucargo.core.logging

import platform.Foundation.NSLog

actual fun logError(tag: String, message: String) {
    NSLog("ERROR [%@]: %@", tag, message)
}
