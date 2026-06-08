package com.juanpablo0612.tucargo.features.auth.presentation.resetpassword

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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.juanpablo0612.tucargo.core.ui.components.ErrorCard
import com.juanpablo0612.tucargo.core.ui.components.RoundedTextField
import com.juanpablo0612.tucargo.core.ui.theme.TuCargoTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import tucargo.composeapp.generated.resources.Res
import tucargo.composeapp.generated.resources.mail
import tucargo.composeapp.generated.resources.network_error
import tucargo.composeapp.generated.resources.reset_password_back_to_login_button
import tucargo.composeapp.generated.resources.reset_password_email_label
import tucargo.composeapp.generated.resources.reset_password_email_placeholder
import tucargo.composeapp.generated.resources.reset_password_email_required_error
import tucargo.composeapp.generated.resources.reset_password_submit_button
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
        onSubmit = viewModel::onSubmit,
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
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.reset_password_back_to_login_button),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        AnimatedContent(
            targetState = uiState.isSuccess,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "ResetPasswordContent",
        ) { isSuccess ->
            if (isSuccess) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 32.dp),
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
                                imageVector = Icons.Default.CheckCircle,
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
                        .padding(innerPadding)
                        .padding(horizontal = 24.dp),
                ) {
                    Spacer(Modifier.height(16.dp))

                    AnimatedVisibility(
                        visible = uiState.error != null,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut(),
                    ) {
                        Column {
                            uiState.error?.let {
                                val errorRes = when (it) {
                                    ResetPasswordError.EmailRequired -> Res.string.reset_password_email_required_error
                                    ResetPasswordError.NetworkError -> Res.string.network_error
                                    ResetPasswordError.UnknownError -> Res.string.unknown_error
                                }
                                ErrorCard(
                                    message = stringResource(errorRes),
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                            Spacer(Modifier.height(16.dp))
                        }
                    }

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

                    Spacer(Modifier.weight(1f))

                    Button(
                        onClick = onSubmit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = MaterialTheme.shapes.large,
                        enabled = !uiState.isLoading,
                    ) {
                        AnimatedContent(
                            targetState = uiState.isLoading,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "ResetButtonContent",
                        ) { loading ->
                            if (loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp,
                                )
                            } else {
                                Text(
                                    text = stringResource(Res.string.reset_password_submit_button),
                                    style = MaterialTheme.typography.titleSmall,
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))
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
