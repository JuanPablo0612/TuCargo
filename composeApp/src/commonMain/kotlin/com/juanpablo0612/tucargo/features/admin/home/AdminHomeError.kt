package com.juanpablo0612.tucargo.features.admin.home

sealed interface AdminHomeError {
    data object LoadError : AdminHomeError
}
