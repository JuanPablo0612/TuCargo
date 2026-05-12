package com.juanpablo0612.tucargo.features.driver.home.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.juanpablo0612.tucargo.features.driver.home.presentation.components.AvailabilityButton
import com.juanpablo0612.tucargo.features.driver.home.presentation.components.BalanceCard
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverHomeScreen(
    viewModel: DriverHomeViewModel = koinViewModel(), // Inyectado vía Koin
    onSignOut: () -> Unit
) {
    // Observamos el estado del ViewModel (Balance, Disponibilidad, Viajes)
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Panel del Conductor", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = if (state.isAvailable) "Estás activo para recibir viajes" else "Desconectado",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (state.isAvailable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                },
                actions = {
                    TextButton(onClick = onSignOut) {
                        Text("Salir", color = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 1. Botón de Disponibilidad (Paso clave de los 11 días)
                item {
                    Spacer(Modifier.height(8.dp))
                    AvailabilityButton(
                        isAvailable = state.isAvailable,
                        onToggle = { viewModel.toggleAvailability(it) }
                    )
                }

                // 2. Tarjeta de Balance y Estadísticas
                item {
                    BalanceCard(
                        balance = state.balance,
                        totalTrips = state.totalTrips
                    )
                }

                // 3. Título de Viajes Activos[cite: 1]
                item {
                    Text(
                        text = "Viajes en curso",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                // 4. Lista de Viajes (Extraídos de Firestore vía ViewModel)[cite: 1]
                if (state.activeTrips.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Text(
                                "No tienes viajes activos en este momento.",
                                modifier = Modifier.padding(24.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(state.activeTrips) { trip ->
                        ActiveTripItem(trip.id) // Puedes pasar el objeto trip completo
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveTripItem(tripId: String) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        ListItem(
            headlineContent = { Text("Viaje ID: ${tripId.take(8)}") },
            supportingContent = { Text("Estado: En curso") },
            trailingContent = {
                Button(onClick = { /* Navegar a detalles */ }) {
                    Text("Ver")
                }
            }
        )
    }
}