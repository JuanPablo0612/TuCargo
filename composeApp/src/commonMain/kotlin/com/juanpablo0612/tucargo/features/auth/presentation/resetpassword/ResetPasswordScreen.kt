package com.juanpablo0612.tucargo.features.auth.presentation.resetpassword

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import com.juanpablo0612.tucargo.core.ui.theme.TuCargoTheme
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import tucargo.composeapp.generated.resources.Res
import tucargo.composeapp.generated.resources.network_error
import tucargo.composeapp.generated.resources.reset_password_email_label
import tucargo.composeapp.generated.resources.reset_password_email_required_error
import tucargo.composeapp.generated.resources.reset_password_submit_button
import tucargo.composeapp.generated.resources.reset_password_success_message
import tucargo.composeapp.generated.resources.reset_password_title
import tucargo.composeapp.generated.resources.unknown_error

@Composable
fun ResetPasswordScreen(
    viewModel: ResetPasswordViewModel = koinViewModel(),
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ResetPasswordScreenContent(
        uiState = uiState,
        emailState = viewModel.emailState,
        onSubmit = viewModel::onSubmit,
        onBackClick = onBackClick
    )
}

@Composable
internal fun ResetPasswordScreenContent(
    uiState: ResetPasswordState,
    emailState: TextFieldState,
    onSubmit: () -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            TextButton(onClick = onBackClick) {
                Text("← ${stringResource(Res.string.reset_password_title)}")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(Res.string.reset_password_title),
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.semantics { heading() }
            )

            if (uiState.isSuccess) {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = stringResource(Res.string.reset_password_success_message),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Spacer(modifier = Modifier.height(16.dp))
                uiState.error?.let {
                    val errorRes = when (it) {
                        ResetPasswordError.EmailRequired -> Res.string.reset_password_email_required_error
                        ResetPasswordError.NetworkError -> Res.string.network_error
                        ResetPasswordError.UnknownError -> Res.string.unknown_error
                    }
                    ErrorCard(
                        message = stringResource(errorRes),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                RoundedTextField(
                    state = emailState,
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(
                            text = stringResource(Res.string.reset_password_email_label),
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done
                    )
                )
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = onSubmit,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = stringResource(Res.string.reset_password_submit_button),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
internal fun ResetPasswordScreenContentPreview() {
    TuCargoTheme {
        ResetPasswordScreenContent(
            uiState = ResetPasswordState(),
            emailState = rememberTextFieldState(),
            onSubmit = {},
            onBackClick = {}
        )
    }
}
