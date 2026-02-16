package com.juanpablo0612.tucargo.features.auth.presentation.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.juanpablo0612.tucargo.core.ui.components.RoundedTextField
import com.juanpablo0612.tucargo.core.ui.components.SecureRoundedTextField
import com.juanpablo0612.tucargo.core.ui.theme.TuCargoTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tucargo.composeapp.generated.resources.Res
import tucargo.composeapp.generated.resources.arrow_forward
import tucargo.composeapp.generated.resources.login_email_error
import tucargo.composeapp.generated.resources.login_email_label
import tucargo.composeapp.generated.resources.login_email_placeholder
import tucargo.composeapp.generated.resources.login_forgot_password_button
import tucargo.composeapp.generated.resources.login_login_button
import tucargo.composeapp.generated.resources.login_password_error
import tucargo.composeapp.generated.resources.login_password_label
import tucargo.composeapp.generated.resources.login_password_placeholder
import tucargo.composeapp.generated.resources.login_subtitle
import tucargo.composeapp.generated.resources.login_title
import tucargo.composeapp.generated.resources.mail
import tucargo.composeapp.generated.resources.motorcycle
import tucargo.composeapp.generated.resources.visibility

@Composable
fun LoginScreen(viewModel: LoginViewModel = viewModel(), onForgotPasswordClick: () -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LoginScreenContent(
        uiState = uiState,
        emailState = viewModel.emailState,
        passwordState = viewModel.passwordState,
        onAction = viewModel::onAction,
        onForgotPasswordClick = onForgotPasswordClick
    )
}

@Composable
internal fun LoginScreenContent(
    uiState: LoginState,
    emailState: TextFieldState,
    passwordState: TextFieldState,
    onAction: (LoginAction) -> Unit,
    onForgotPasswordClick: () -> Unit
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(8.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(shape = MaterialTheme.shapes.medium)
                    .background(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    .padding(16.dp),
            ) {
                Icon(
                    painter = painterResource(Res.drawable.motorcycle),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = stringResource(Res.string.login_title),
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(Res.string.login_subtitle),
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            RoundedTextField(
                state = emailState,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(
                        text = stringResource(Res.string.login_email_label),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                placeholder = {
                    Text(text = stringResource(Res.string.login_email_placeholder))
                },
                trailingIcon = {
                    Icon(
                        painter = painterResource(Res.drawable.mail),
                        contentDescription = null
                    )
                },
                supportingText = if (!uiState.isEmailValid) {
                    {
                        Text(text = stringResource(Res.string.login_email_error))
                    }
                } else null,
                isError = !uiState.isEmailValid,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                onKeyboardAction = { performDefaultAction ->
                    onAction(LoginAction.ValidateEmail)
                    performDefaultAction()
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            SecureRoundedTextField(
                state = passwordState,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(
                        text = stringResource(Res.string.login_password_label),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                placeholder = {
                    Text(text = stringResource(Res.string.login_password_placeholder))
                },
                trailingIcon = {
                    Icon(
                        painter = painterResource(Res.drawable.visibility),
                        contentDescription = null
                    )
                },
                supportingText = if (!uiState.isPasswordValid) {
                    {
                        Text(text = stringResource(Res.string.login_password_error))
                    }
                } else null,
                isError = !uiState.isPasswordValid,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                onKeyboardAction = { performDefaultAction ->
                    onAction(LoginAction.ValidatePassword)
                    performDefaultAction()
                }
            )
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = { onAction(LoginAction.Login) },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = stringResource(Res.string.login_login_button),
                    modifier = Modifier.padding(vertical = 8.dp),
                )
                Icon(
                    painter = painterResource(Res.drawable.arrow_forward),
                    contentDescription = null
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = onForgotPasswordClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(Res.string.login_forgot_password_button))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenContentPreview() {
    TuCargoTheme {
        LoginScreenContent(
            uiState = LoginState(),
            emailState = rememberTextFieldState(),
            passwordState = rememberTextFieldState(),
            onAction = {},
            onForgotPasswordClick = {}
        )
    }
}
