package com.juanpablo0612.tucargo.features.auth.presentation.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecureTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldLabelPosition
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.juanpablo0612.tucargo.core.ui.theme.TuCargoTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun LoginScreenContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .safeContentPadding()
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Welcome Back",
            modifier = Modifier.padding(bottom = 16.dp),
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "Log in to access your courier dashboard and start delivering.",
            modifier = Modifier.padding(bottom = 32.dp),
            style = MaterialTheme.typography.bodyLarge
        )
        TextField(
            state = rememberTextFieldState(),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            labelPosition = TextFieldLabelPosition.Above(),
            label = {
                Text(text = "Email Address", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
            },
            placeholder = {
                Text(text = "courier@tucargo.com")
            }
        )
        SecureTextField(
            state = rememberTextFieldState(),
            modifier = Modifier.fillMaxWidth(),
            labelPosition = TextFieldLabelPosition.Above(),
            label = {
                Text(text = "Password", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
            },
            placeholder = {
                Text(text = "********")
            }
        )
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text(
                text = "Log In",
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }
        TextButton(
            onClick = {},
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Forgot Password?")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenContentPreview() {
    TuCargoTheme {
        LoginScreenContent()
    }
}