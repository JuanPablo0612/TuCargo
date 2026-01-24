package com.juanpablo0612.tucargo.features.auth.presentation.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.juanpablo0612.tucargo.core.ui.components.RoundedTextField
import com.juanpablo0612.tucargo.core.ui.components.SecureRoundedTextField
import com.juanpablo0612.tucargo.core.ui.theme.TuCargoTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import tucargo.composeapp.generated.resources.Res
import tucargo.composeapp.generated.resources.arrow_forward
import tucargo.composeapp.generated.resources.login_email_label
import tucargo.composeapp.generated.resources.login_email_placeholder
import tucargo.composeapp.generated.resources.login_forgot_password_button
import tucargo.composeapp.generated.resources.login_login_button
import tucargo.composeapp.generated.resources.login_password_label
import tucargo.composeapp.generated.resources.login_password_placeholder
import tucargo.composeapp.generated.resources.login_subtitle
import tucargo.composeapp.generated.resources.login_title
import tucargo.composeapp.generated.resources.mail
import tucargo.composeapp.generated.resources.motorcycle
import tucargo.composeapp.generated.resources.visibility

@Composable
fun LoginScreen(viewModel: LoginViewModel = viewModel(), onForgotPasswordClick: () -> Unit) {
    LoginScreenContent(
        emailState = viewModel.emailState,
        passwordState = viewModel.passwordState,
        onLoginClick = viewModel::onLogin,
        onForgotPasswordClick = onForgotPasswordClick
    )
}

@Composable
fun LoginScreenContent(
    emailState: TextFieldState,
    passwordState: TextFieldState,
    onLoginClick: () -> Unit,
    onForgotPasswordClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
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
            modifier = Modifier.padding(bottom = 16.dp),
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = stringResource(Res.string.login_subtitle),
            modifier = Modifier.padding(bottom = 32.dp),
            style = MaterialTheme.typography.bodyLarge
        )
        RoundedTextField(
            state = emailState,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
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
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
        )
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
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done)
        )
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onLoginClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
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
        TextButton(
            onClick = onForgotPasswordClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(Res.string.login_forgot_password_button))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenContentPreview() {
    TuCargoTheme {
        LoginScreenContent(
            emailState = rememberTextFieldState(),
            passwordState = rememberTextFieldState(),
            onLoginClick = {},
            onForgotPasswordClick = {}
        )
    }
}
