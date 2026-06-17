package com.juanpablo0612.tucargo.features.auth.resetpassword

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import tucargo.composeapp.generated.resources.arrow_back
import tucargo.composeapp.generated.resources.check_circle
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.juanpablo0612.tucargo.core.ui.components.ResponsiveContainer
import com.juanpablo0612.tucargo.core.ui.theme.LocalDimensions
import com.juanpablo0612.tucargo.core.ui.asString
import com.juanpablo0612.tucargo.core.ui.components.ErrorBanner
import com.juanpablo0612.tucargo.core.ui.components.LoadingButton
import com.juanpablo0612.tucargo.core.ui.components.RoundedTextField
import com.juanpablo0612.tucargo.core.ui.theme.TuCargoTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import tucargo.composeapp.generated.resources.Res
import tucargo.composeapp.generated.resources.lock_24px
import tucargo.composeapp.generated.resources.mail
import tucargo.composeapp.generated.resources.network_error
import tucargo.composeapp.generated.resources.reset_password_back_to_login_button
import tucargo.composeapp.generated.resources.reset_password_email_label
import tucargo.composeapp.generated.resources.reset_password_email_placeholder
import tucargo.composeapp.generated.resources.reset_password_submit_button
import tucargo.composeapp.generated.resources.reset_password_subtitle
import tucargo.composeapp.generated.resources.reset_password_success_message
import tucargo.composeapp.generated.resources.reset_password_title
import tucargo.composeapp.generated.resources.unknown_error

@Composable
fun ResetPasswordScreen(
    viewModel: ResetPasswordViewModel = koinViewModel(),
    onBackClick: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ResetPasswordScreenContent(
        uiState = uiState,
        emailState = viewModel.emailState,
        onSubmit = { viewModel.onAction(ResetPasswordAction.Submit) },
        onBackClick = onBackClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ResetPasswordScreenContent(
    uiState: ResetPasswordState,
    emailState: TextFieldState,
    onSubmit: () -> Unit,
    onBackClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.reset_password_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(Res.drawable.arrow_back),
                            contentDescription = stringResource(Res.string.reset_password_back_to_login_button),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        val dimensions = LocalDimensions.current
        ResponsiveContainer(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
        AnimatedContent(
            targetState = uiState.isSuccess,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "ResetPasswordContent",
        ) { isSuccess ->
            if (isSuccess) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = dimensions.formHorizontalPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(MaterialTheme.shapes.extraLarge)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.check_circle),
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                        Text(
                            text = stringResource(Res.string.reset_password_success_message),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                        Button(
                            onClick = onBackClick,
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.large,
                        ) {
                            Text(
                                text = stringResource(Res.string.reset_password_back_to_login_button),
                                style = MaterialTheme.typography.titleSmall,
                            )
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = dimensions.formHorizontalPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(Modifier.height(32.dp))

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(MaterialTheme.shapes.extraLarge)
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.lock_24px),
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    Text(
                        text = stringResource(Res.string.reset_password_subtitle),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(Modifier.height(32.dp))

                    ErrorBanner(
                        message = uiState.authError?.let {
                            val errorRes = when (it) {
                                ResetPasswordError.NetworkError -> Res.string.network_error
                                ResetPasswordError.UnknownError -> Res.string.unknown_error
                            }
                            stringResource(errorRes)
                        }
                    )

                    RoundedTextField(
                        state = emailState,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(Res.string.reset_password_email_label)) },
                        placeholder = { Text(stringResource(Res.string.reset_password_email_placeholder)) },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(Res.drawable.mail),
                                contentDescription = null,
                            )
                        },
                        isError = uiState.emailError != null,
                        supportingText = uiState.emailError?.let { err -> { Text(err.asString()) } },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            autoCorrectEnabled = false,
                            imeAction = ImeAction.Done,
                        ),
                        onKeyboardAction = { performDefaultAction ->
                            onSubmit()
                            performDefaultAction()
                        },
                    )

                    Spacer(Modifier.height(32.dp))

                    LoadingButton(
                        onClick = onSubmit,
                        isLoading = uiState.isLoading,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = stringResource(Res.string.reset_password_submit_button),
                            style = MaterialTheme.typography.titleSmall,
                        )
                    }
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
            onBackClick = {},
        )
    }
}
