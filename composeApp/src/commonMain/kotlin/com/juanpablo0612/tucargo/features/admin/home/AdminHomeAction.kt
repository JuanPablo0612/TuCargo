package com.juanpablo0612.tucargo.features.admin.home

sealed interface AdminHomeAction {
    data object Refresh : AdminHomeAction
}
