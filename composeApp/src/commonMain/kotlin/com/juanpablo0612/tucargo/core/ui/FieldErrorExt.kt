package com.juanpablo0612.tucargo.core.ui

import androidx.compose.runtime.Composable
import com.juanpablo0612.tucargo.core.validation.FieldError
import org.jetbrains.compose.resources.stringResource
import tucargo.composeapp.generated.resources.Res
import tucargo.composeapp.generated.resources.validation_email_invalid
import tucargo.composeapp.generated.resources.validation_email_required
import tucargo.composeapp.generated.resources.validation_id_back_required
import tucargo.composeapp.generated.resources.validation_id_front_required
import tucargo.composeapp.generated.resources.validation_name_required
import tucargo.composeapp.generated.resources.validation_password_required
import tucargo.composeapp.generated.resources.validation_password_too_short
import tucargo.composeapp.generated.resources.validation_password_weak
import tucargo.composeapp.generated.resources.validation_phone_invalid
import tucargo.composeapp.generated.resources.validation_phone_required

@Composable
fun FieldError.asString(): String = stringResource(
    when (this) {
        FieldError.EmailRequired -> Res.string.validation_email_required
        FieldError.EmailInvalid -> Res.string.validation_email_invalid
        FieldError.PasswordRequired -> Res.string.validation_password_required
        FieldError.PasswordTooShort -> Res.string.validation_password_too_short
        FieldError.PasswordWeak -> Res.string.validation_password_weak
        FieldError.NameRequired -> Res.string.validation_name_required
        FieldError.PhoneRequired -> Res.string.validation_phone_required
        FieldError.PhoneInvalid -> Res.string.validation_phone_invalid
        FieldError.IdFrontRequired -> Res.string.validation_id_front_required
        FieldError.IdBackRequired -> Res.string.validation_id_back_required
    }
)
