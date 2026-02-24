package com.juanpablo0612.tucargo.features.auth.presentation.register

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
// Importamos tus componentes personalizados desde la carpeta core
import com.juanpablo0612.tucargo.core.ui.components.RoundedTextField
import com.juanpablo0612.tucargo.core.ui.components.SecureRoundedTextField

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    RegisterScreenContent(
        nameState = viewModel.nameState,
        emailState = viewModel.emailState,
        passwordState = viewModel.passwordState,
        confirmPasswordState = viewModel.confirmPasswordState,
        onRegisterClick = viewModel::onRegister,
        onBackClick = onBackClick
    )
}

@Composable
fun RegisterScreenContent(
    nameState: TextFieldState,
    emailState: TextFieldState,
    passwordState: TextFieldState,
    confirmPasswordState: TextFieldState,
    onRegisterClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding() // Evita que el contenido choque con la muesca o barra de estado
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        TextButton(onClick = onBackClick) {
            Text("< Volver")
        }

        Text(
            text = "Crea tu cuenta",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(modifier = Modifier.height(24.dp))

        RoundedTextField(
            state = nameState,
            label = { Text("Nombre Completo") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        RoundedTextField(
            state = emailState,
            label = { Text("Correo Electrónico") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )

        SecureRoundedTextField(
            state = passwordState,
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            )
        )

        SecureRoundedTextField(
            state = confirmPasswordState,
            label = { Text("Confirmar Contraseña") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            )
        )

        Button(
            onClick = onRegisterClick,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Registrarse")
        }
    }
}