package com.juanpablo0612.tucargo.core.ui

import androidx.compose.runtime.Composable
import com.juanpablo0612.tucargo.core.validation.FieldError
import org.jetbrains.compose.resources.stringResource
import tucargo.composeapp.generated.resources.Res
import tucargo.composeapp.generated.resources.validation_address_required
import tucargo.composeapp.generated.resources.validation_cargo_description_required
import tucargo.composeapp.generated.resources.validation_document_required
import tucargo.composeapp.generated.resources.validation_email_invalid
import tucargo.composeapp.generated.resources.validation_email_required
import tucargo.composeapp.generated.resources.validation_name_required
import tucargo.composeapp.generated.resources.validation_password_required
import tucargo.composeapp.generated.resources.validation_password_too_short
import tucargo.composeapp.generated.resources.validation_password_weak
import tucargo.composeapp.generated.resources.validation_phone_invalid
import tucargo.composeapp.generated.resources.validation_phone_required
import tucargo.composeapp.generated.resources.validation_plate_invalid
import tucargo.composeapp.generated.resources.validation_plate_required
import tucargo.composeapp.generated.resources.validation_vehicle_color_required
import tucargo.composeapp.generated.resources.validation_vehicle_model_required
import tucargo.composeapp.generated.resources.validation_vehicle_year_invalid
import tucargo.composeapp.generated.resources.validation_vehicle_year_required

@Composable
fun FieldError.asString(): String = when (this) {
    FieldError.EmailRequired -> stringResource(Res.string.validation_email_required)
    FieldError.EmailInvalid -> stringResource(Res.string.validation_email_invalid)
    FieldError.PasswordRequired -> stringResource(Res.string.validation_password_required)
    FieldError.PasswordTooShort -> stringResource(Res.string.validation_password_too_short)
    FieldError.PasswordWeak -> stringResource(Res.string.validation_password_weak)
    FieldError.NameRequired -> stringResource(Res.string.validation_name_required)
    FieldError.PhoneRequired -> stringResource(Res.string.validation_phone_required)
    FieldError.PhoneInvalid -> stringResource(Res.string.validation_phone_invalid)
    FieldError.PlateRequired -> stringResource(Res.string.validation_plate_required)
    FieldError.PlateInvalid -> stringResource(Res.string.validation_plate_invalid)
    FieldError.VehicleModelRequired -> stringResource(Res.string.validation_vehicle_model_required)
    FieldError.VehicleColorRequired -> stringResource(Res.string.validation_vehicle_color_required)
    FieldError.VehicleYearRequired -> stringResource(Res.string.validation_vehicle_year_required)
    FieldError.VehicleYearInvalid -> stringResource(Res.string.validation_vehicle_year_invalid)
    FieldError.AddressRequired -> stringResource(Res.string.validation_address_required)
    FieldError.CargoDescriptionRequired -> stringResource(Res.string.validation_cargo_description_required)
    is FieldError.DocumentRequired -> stringResource(Res.string.validation_document_required)
}
