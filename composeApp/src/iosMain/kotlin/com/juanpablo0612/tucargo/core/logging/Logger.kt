package com.juanpablo0612.tucargo.core.logging

actual fun logError(tag: String, message: String) {
    println("ERROR [$tag]: $message")
}
