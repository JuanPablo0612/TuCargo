package com.juanpablo0612.tucargo.features.auth.presentation.resetpassword

sealed interface ResetPasswordAction {
    data object Submit : ResetPasswordAction
}
