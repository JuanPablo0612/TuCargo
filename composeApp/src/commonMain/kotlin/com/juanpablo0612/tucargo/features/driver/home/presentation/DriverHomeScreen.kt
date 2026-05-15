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
import org.jetbrains.compose.resources.stringResource
import tucargo.composeapp.generated.resources.Res
import tucargo.composeapp.generated.resources.driver_home_active_desc
import tucargo.composeapp.generated.resources.driver_home_active_trips_title
import tucargo.composeapp.generated.resources.driver_home_empty_trips_message
import tucargo.composeapp.generated.resources.driver_home_offline_desc
import tucargo.composeapp.generated.resources.driver_home_sign_out_button
import tucargo.composeapp.generated.resources.driver_home_title
import tucargo.composeapp.generated.resources.driver_home_trip_id_label
import tucargo.composeapp.generated.resources.driver_home_trip_status_in_progress
import tucargo.composeapp.generated.resources.driver_home_view_trip_button
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
                        Text(stringResource(Res.string.driver_home_title), style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = if (state.isAvailable) stringResource(Res.string.driver_home_active_desc) else stringResource(Res.string.driver_home_offline_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (state.isAvailable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                },
                actions = {
                    TextButton(onClick = onSignOut) {
                        Text(stringResource(Res.string.driver_home_sign_out_button), color = MaterialTheme.colorScheme.error)
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
                        text = stringResource(Res.string.driver_home_active_trips_title),
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
                                stringResource(Res.string.driver_home_empty_trips_message),
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
            headlineContent = { Text(stringResource(Res.string.driver_home_trip_id_label, tripId.take(8))) },
            supportingContent = { Text(stringResource(Res.string.driver_home_trip_status_in_progress)) },
            trailingContent = {
                Button(onClick = { /* Navegar a detalles */ }) {
                    Text(stringResource(Res.string.driver_home_view_trip_button))
                }
            }
        )
    }
}
