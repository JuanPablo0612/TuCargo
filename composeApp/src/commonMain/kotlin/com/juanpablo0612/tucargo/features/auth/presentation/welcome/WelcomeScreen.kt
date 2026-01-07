package com.juanpablo0612.tucargo.features.auth.presentation.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.juanpablo0612.tucargo.core.ui.theme.TuCargoTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import tucargo.composeapp.generated.resources.Res
import tucargo.composeapp.generated.resources.local_shipping
import tucargo.composeapp.generated.resources.motorcycle
import tucargo.composeapp.generated.resources.package_2

@Composable
fun WelcomeScreenContent() {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding(),
        color = MaterialTheme.colorScheme.primary
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.large)
                    .background(color = MaterialTheme.colorScheme.background)
                    .padding(16.dp)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.local_shipping),
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = "TuCargo",
                modifier = Modifier.padding(top = 16.dp),
                style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Motorcycle Logistics",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background, contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.package_2),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(text = "I need to send cargo", modifier = Modifier.padding(vertical = 8.dp))
                Spacer(modifier = Modifier.weight(1f))
            }
            OutlinedButton(
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onPrimary)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.motorcycle),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(text = "I am a driver", modifier = Modifier.padding(vertical = 8.dp))
                Spacer(modifier = Modifier.weight(1f))
            }
            Text(text = "Version 1.0.0", modifier = Modifier.padding(vertical = 32.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeScreenContentPreview() {
    TuCargoTheme {
        WelcomeScreenContent()
    }
}