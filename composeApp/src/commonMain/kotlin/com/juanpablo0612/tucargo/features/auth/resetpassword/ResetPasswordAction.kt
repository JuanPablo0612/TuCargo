package com.juanpablo0612.tucargo.features.auth.resetpassword

sealed interface ResetPasswordAction {
    data object Submit : ResetPasswordAction
}
