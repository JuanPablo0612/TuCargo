package com.juanpablo0612.tucargo.features.auth.presentation.register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.juanpablo0612.tucargo.core.ui.components.ErrorCard
import com.juanpablo0612.tucargo.core.ui.components.RoundedTextField
import com.juanpablo0612.tucargo.core.ui.components.SecureRoundedTextField
import com.juanpablo0612.tucargo.core.ui.theme.TuCargoTheme
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import tucargo.composeapp.generated.resources.Res
import tucargo.composeapp.generated.resources.network_error
import tucargo.composeapp.generated.resources.register_back_button
import tucargo.composeapp.generated.resources.register_button
import tucargo.composeapp.generated.resources.register_email_label
import tucargo.composeapp.generated.resources.register_fields_required
import tucargo.composeapp.generated.resources.register_invalid_email_error
import tucargo.composeapp.generated.resources.register_invalid_phone_error
import tucargo.composeapp.generated.resources.register_name_label
import tucargo.composeapp.generated.resources.register_password_label
import tucargo.composeapp.generated.resources.register_password_too_short
import tucargo.composeapp.generated.resources.register_phone_label
import tucargo.composeapp.generated.resources.register_role_client
import tucargo.composeapp.generated.resources.register_role_driver
import tucargo.composeapp.generated.resources.register_role_label
import tucargo.composeapp.generated.resources.register_title
import tucargo.composeapp.generated.resources.register_user_already_exists
import tucargo.composeapp.generated.resources.register_weak_password_error
import tucargo.composeapp.generated.resources.unknown_error

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = koinViewModel(),
    onBackClick: () -> Unit,
    onRegisterSuccess: (role: String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    uiState.navigationEvent?.consume()?.let { role -> onRegisterSuccess(role) }

    RegisterScreenContent(
        uiState = uiState,
        nameState = viewModel.nameState,
        emailState = viewModel.emailState,
        phoneState = viewModel.phoneState,
        passwordState = viewModel.passwordState,
        selectedRole = viewModel.selectedRole,
        onRoleSelected = { viewModel.selectedRole = it },
        onRegisterClick = viewModel::onRegister,
        onBackClick = onBackClick
    )
}

@Composable
internal fun RegisterScreenContent(
    uiState: RegisterState,
    nameState: TextFieldState,
    emailState: TextFieldState,
    phoneState: TextFieldState,
    passwordState: TextFieldState,
    selectedRole: String,
    onRoleSelected: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            TextButton(onClick = onBackClick) {
                Text(stringResource(Res.string.register_back_button))
            }
            Text(
                text = stringResource(Res.string.register_title),
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.semantics { heading() }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(Res.string.register_role_label),
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterChip(
                    selected = selectedRole == "CLIENT",
                    onClick = { onRoleSelected("CLIENT") },
                    label = { Text(stringResource(Res.string.register_role_client)) }
                )
                FilterChip(
                    selected = selectedRole == "DRIVER",
                    onClick = { onRoleSelected("DRIVER") },
                    label = { Text(stringResource(Res.string.register_role_driver)) }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            uiState.error?.let {
                val errorRes = when (it) {
                    RegisterError.FieldsRequired -> Res.string.register_fields_required
                    RegisterError.PasswordTooShort -> Res.string.register_password_too_short
                    RegisterError.WeakPassword -> Res.string.register_weak_password_error
                    RegisterError.InvalidEmailFormat -> Res.string.register_invalid_email_error
                    RegisterError.InvalidPhoneFormat -> Res.string.register_invalid_phone_error
                    RegisterError.UserAlreadyExists -> Res.string.register_user_already_exists
                    RegisterError.NetworkError -> Res.string.network_error
                    RegisterError.UnknownError -> Res.string.unknown_error
                }
                ErrorCard(message = stringResource(errorRes), modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(12.dp))
            }
            RoundedTextField(
                state = nameState,
                label = { Text(stringResource(Res.string.register_name_label)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            RoundedTextField(
                state = emailState,
                label = { Text(stringResource(Res.string.register_email_label)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )
            RoundedTextField(
                state = phoneState,
                label = { Text(stringResource(Res.string.register_phone_label)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                )
            )
            SecureRoundedTextField(
                state = passwordState,
                label = { Text(stringResource(Res.string.register_password_label)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                )
            )
            Button(
                onClick = onRegisterClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(stringResource(Res.string.register_button))
                }
            }
        }
    }
}

@Preview
@Composable
internal fun RegisterScreenContentPreview() {
    TuCargoTheme {
        RegisterScreenContent(
            uiState = RegisterState(),
            nameState = rememberTextFieldState(),
            emailState = rememberTextFieldState(),
            phoneState = rememberTextFieldState(),
            passwordState = rememberTextFieldState(),
            selectedRole = "CLIENT",
            onRoleSelected = {},
            onRegisterClick = {},
            onBackClick = {}
        )
    }
}
