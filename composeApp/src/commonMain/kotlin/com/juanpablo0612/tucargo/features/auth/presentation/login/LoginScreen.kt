package com.juanpablo0612.tucargo.features.auth.presentation.login

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import tucargo.composeapp.generated.resources.Res
import tucargo.composeapp.generated.resources.arrow_forward
import tucargo.composeapp.generated.resources.cd_toggle_password_visibility
import tucargo.composeapp.generated.resources.local_shipping
import tucargo.composeapp.generated.resources.lock_24px
import tucargo.composeapp.generated.resources.login_email_error
import tucargo.composeapp.generated.resources.login_email_label
import tucargo.composeapp.generated.resources.login_email_placeholder
import tucargo.composeapp.generated.resources.login_forgot_password_button
import tucargo.composeapp.generated.resources.login_invalid_credentials_error
import tucargo.composeapp.generated.resources.login_login_button
import tucargo.composeapp.generated.resources.login_password_error
import tucargo.composeapp.generated.resources.login_password_label
import tucargo.composeapp.generated.resources.login_password_placeholder
import tucargo.composeapp.generated.resources.login_register_link
import tucargo.composeapp.generated.resources.login_subtitle
import tucargo.composeapp.generated.resources.login_title
import tucargo.composeapp.generated.resources.mail
import tucargo.composeapp.generated.resources.network_error
import tucargo.composeapp.generated.resources.unknown_error
import tucargo.composeapp.generated.resources.visibility
import tucargo.composeapp.generated.resources.visibility_off

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = koinViewModel(),
    onForgotPasswordClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onLoginSuccess: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    uiState.navigationEvent?.consume()?.let { role -> onLoginSuccess(role) }

    LoginScreenContent(
        uiState = uiState,
        emailState = viewModel.emailState,
        passwordState = viewModel.passwordState,
        onAction = viewModel::onAction,
        onForgotPasswordClick = onForgotPasswordClick,
        onRegisterClick = onRegisterClick,
    )
}

@Composable
internal fun LoginScreenContent(
    uiState: LoginState,
    emailState: TextFieldState,
    passwordState: TextFieldState,
    onAction: (LoginAction) -> Unit,
    onForgotPasswordClick: () -> Unit,
    onRegisterClick: () -> Unit,
) {
    var isPasswordVisible by remember { mutableStateOf(false) }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
        ) {
            Spacer(Modifier.height(48.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(80.dp)
                    .clip(MaterialTheme.shapes.extraLarge)
                    .background(MaterialTheme.colorScheme.primaryContainer),
            ) {
                Icon(
                    painter = painterResource(Res.drawable.local_shipping),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }

            Spacer(Modifier.height(28.dp))

            Text(
                text = stringResource(Res.string.login_title),
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.semantics { heading() },
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(Res.string.login_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(24.dp))

            AnimatedVisibility(
                visible = uiState.loginError != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Column {
                    uiState.loginError?.let {
                        val errorRes = when (it) {
                            LoginError.InvalidCredentials -> Res.string.login_invalid_credentials_error
                            LoginError.NetworkError -> Res.string.network_error
                            LoginError.UnknownError -> Res.string.unknown_error
                        }
                        ErrorCard(
                            message = stringResource(errorRes),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                RoundedTextField(
                    state = emailState,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(Res.string.login_email_label)) },
                    placeholder = { Text(stringResource(Res.string.login_email_placeholder)) },
                    leadingIcon = {
                        Icon(painterResource(Res.drawable.mail), contentDescription = null)
                    },
                    supportingText = if (!uiState.isEmailValid) {
                        { Text(stringResource(Res.string.login_email_error)) }
                    } else null,
                    isError = !uiState.isEmailValid,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        autoCorrectEnabled = false,
                        imeAction = ImeAction.Next,
                    ),
                    onKeyboardAction = { performDefaultAction ->
                        onAction(LoginAction.ValidateEmail)
                        performDefaultAction()
                    },
                )

                SecureRoundedTextField(
                    state = passwordState,
                    modifier = Modifier.fillMaxWidth(),
                    textObfuscationMode = if (isPasswordVisible) TextObfuscationMode.Visible else TextObfuscationMode.Hidden,
                    label = { Text(stringResource(Res.string.login_password_label)) },
                    placeholder = { Text(stringResource(Res.string.login_password_placeholder)) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(Res.drawable.lock_24px),
                            contentDescription = null,
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                painter = painterResource(
                                    if (isPasswordVisible) Res.drawable.visibility_off
                                    else Res.drawable.visibility
                                ),
                                contentDescription = stringResource(Res.string.cd_toggle_password_visibility),
                            )
                        }
                    },
                    supportingText = if (!uiState.isPasswordValid) {
                        { Text(stringResource(Res.string.login_password_error)) }
                    } else null,
                    isError = !uiState.isPasswordValid,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                    onKeyboardAction = { performDefaultAction ->
                        onAction(LoginAction.ValidatePassword)
                        performDefaultAction()
                    },
                )
            }

            TextButton(
                onClick = onForgotPasswordClick,
                modifier = Modifier.align(Alignment.End),
            ) {
                Text(stringResource(Res.string.login_forgot_password_button))
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = { onAction(LoginAction.Login) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.large,
                enabled = !uiState.isLoading,
            ) {
                AnimatedContent(
                    targetState = uiState.isLoading,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "LoginButtonContent",
                ) { loading ->
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(Res.string.login_login_button),
                                style = MaterialTheme.typography.titleSmall,
                            )
                            Icon(
                                painterResource(Res.drawable.arrow_forward),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                TextButton(onClick = onRegisterClick) {
                    Text(stringResource(Res.string.login_register_link))
                }
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Preview
@Composable
internal fun LoginScreenContentPreview() {
    TuCargoTheme {
        LoginScreenContent(
            uiState = LoginState(),
            emailState = rememberTextFieldState(),
            passwordState = rememberTextFieldState(),
            onAction = {},
            onForgotPasswordClick = {},
            onRegisterClick = {},
        )
    }
}
