package com.juanpablo0612.tucargo.features.auth.presentation.register

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
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
import tucargo.composeapp.generated.resources.call_24px
import tucargo.composeapp.generated.resources.cd_toggle_password_visibility
import tucargo.composeapp.generated.resources.lock_24px
import tucargo.composeapp.generated.resources.mail
import tucargo.composeapp.generated.resources.network_error
import tucargo.composeapp.generated.resources.person_24px
import tucargo.composeapp.generated.resources.register_back_button
import tucargo.composeapp.generated.resources.register_button
import tucargo.composeapp.generated.resources.register_email_label
import tucargo.composeapp.generated.resources.register_email_placeholder
import tucargo.composeapp.generated.resources.register_fields_required
import tucargo.composeapp.generated.resources.register_invalid_email_error
import tucargo.composeapp.generated.resources.register_invalid_phone_error
import tucargo.composeapp.generated.resources.register_name_label
import tucargo.composeapp.generated.resources.register_name_placeholder
import tucargo.composeapp.generated.resources.register_password_label
import tucargo.composeapp.generated.resources.register_password_placeholder
import tucargo.composeapp.generated.resources.register_password_too_short
import tucargo.composeapp.generated.resources.register_phone_label
import tucargo.composeapp.generated.resources.register_phone_placeholder
import tucargo.composeapp.generated.resources.register_role_client
import tucargo.composeapp.generated.resources.register_role_driver
import tucargo.composeapp.generated.resources.register_role_label
import tucargo.composeapp.generated.resources.register_title
import tucargo.composeapp.generated.resources.register_user_already_exists
import tucargo.composeapp.generated.resources.register_weak_password_error
import tucargo.composeapp.generated.resources.unknown_error
import tucargo.composeapp.generated.resources.visibility
import tucargo.composeapp.generated.resources.visibility_off

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = koinViewModel(),
    onBackClick: () -> Unit,
    onRegisterSuccess: (role: String) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.navigationEvent) {
        uiState.navigationEvent?.consume()?.let { role -> onRegisterSuccess(role) }
    }

    RegisterScreenContent(
        uiState = uiState,
        nameState = viewModel.nameState,
        emailState = viewModel.emailState,
        phoneState = viewModel.phoneState,
        passwordState = viewModel.passwordState,
        selectedRole = viewModel.selectedRole,
        onRoleSelected = { viewModel.selectedRole = it },
        onRegisterClick = viewModel::onRegister,
        onBackClick = onBackClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
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
    onBackClick: () -> Unit,
) {
    var isPasswordVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.register_title),
                        modifier = Modifier.semantics { heading() },
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.register_back_button),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Spacer(Modifier.height(4.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(Res.string.register_role_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = selectedRole == "CLIENT",
                        onClick = { onRoleSelected("CLIENT") },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        label = { Text(stringResource(Res.string.register_role_client)) },
                    )
                    SegmentedButton(
                        selected = selectedRole == "DRIVER",
                        onClick = { onRoleSelected("DRIVER") },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        label = { Text(stringResource(Res.string.register_role_driver)) },
                    )
                }
            }

            AnimatedVisibility(
                visible = uiState.error is RegisterError.UserAlreadyExists
                        || uiState.error is RegisterError.NetworkError
                        || uiState.error is RegisterError.UnknownError,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                uiState.error?.let {
                    val errorRes = when (it) {
                        RegisterError.UserAlreadyExists -> Res.string.register_user_already_exists
                        RegisterError.NetworkError -> Res.string.network_error
                        else -> Res.string.unknown_error
                    }
                    ErrorCard(
                        message = stringResource(errorRes),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                RoundedTextField(
                    state = nameState,
                    label = { Text(stringResource(Res.string.register_name_label)) },
                    placeholder = { Text(stringResource(Res.string.register_name_placeholder)) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(Res.drawable.person_24px),
                            contentDescription = null,
                        )
                    },
                    isError = uiState.isNameError,
                    supportingText = if (uiState.isNameError) {
                        { Text(stringResource(Res.string.register_fields_required)) }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        capitalization = KeyboardCapitalization.Words,
                        autoCorrectEnabled = false,
                        imeAction = ImeAction.Next,
                    ),
                )
                RoundedTextField(
                    state = emailState,
                    label = { Text(stringResource(Res.string.register_email_label)) },
                    placeholder = { Text(stringResource(Res.string.register_email_placeholder)) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(Res.drawable.mail),
                            contentDescription = null,
                        )
                    },
                    isError = uiState.isEmailError,
                    supportingText = if (uiState.isEmailError) {
                        { Text(stringResource(Res.string.register_invalid_email_error)) }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        autoCorrectEnabled = false,
                        imeAction = ImeAction.Next,
                    ),
                )
                RoundedTextField(
                    state = phoneState,
                    label = { Text(stringResource(Res.string.register_phone_label)) },
                    placeholder = { Text(stringResource(Res.string.register_phone_placeholder)) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(Res.drawable.call_24px),
                            contentDescription = null,
                        )
                    },
                    isError = uiState.isPhoneError,
                    supportingText = if (uiState.isPhoneError) {
                        { Text(stringResource(Res.string.register_invalid_phone_error)) }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        autoCorrectEnabled = false,
                        imeAction = ImeAction.Next,
                    ),
                )
                SecureRoundedTextField(
                    state = passwordState,
                    textObfuscationMode = if (isPasswordVisible) TextObfuscationMode.Visible else TextObfuscationMode.Hidden,
                    label = { Text(stringResource(Res.string.register_password_label)) },
                    placeholder = { Text(stringResource(Res.string.register_password_placeholder)) },
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
                    isError = uiState.isPasswordError,
                    supportingText = if (uiState.isPasswordError) {
                        {
                            Text(
                                stringResource(
                                    when (uiState.error) {
                                        RegisterError.PasswordTooShort -> Res.string.register_password_too_short
                                        else -> Res.string.register_weak_password_error
                                    }
                                )
                            )
                        }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                    onKeyboardAction = { performDefaultAction ->
                        onRegisterClick()
                        performDefaultAction()
                    },
                )
            }

            Button(
                onClick = onRegisterClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.large,
                enabled = !uiState.isLoading,
            ) {
                AnimatedContent(
                    targetState = uiState.isLoading,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "RegisterButtonContent",
                ) { loading ->
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(
                            text = stringResource(Res.string.register_button),
                            style = MaterialTheme.typography.titleSmall,
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
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
            onBackClick = {},
        )
    }
}
