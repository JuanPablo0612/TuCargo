package com.juanpablo0612.tucargo.core.ui.event

class UiEvent<out T>(private val value: T) {
    private var consumed = false

    fun consume(): T? = if (consumed) null else value.also { consumed = true }
}
