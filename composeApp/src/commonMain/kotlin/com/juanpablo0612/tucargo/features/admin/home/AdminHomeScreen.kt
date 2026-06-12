package com.juanpablo0612.tucargo.features.admin.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.juanpablo0612.tucargo.core.ui.components.ErrorCard
import com.juanpablo0612.tucargo.domain.model.User
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import tucargo.composeapp.generated.resources.Res
import tucargo.composeapp.generated.resources.admin_home_empty
import tucargo.composeapp.generated.resources.admin_home_load_error
import tucargo.composeapp.generated.resources.admin_home_plate
import tucargo.composeapp.generated.resources.admin_home_subtitle
import tucargo.composeapp.generated.resources.admin_home_title
import tucargo.composeapp.generated.resources.client_home_sign_out_desc

@Composable
fun AdminHomeScreen(
    viewModel: AdminHomeViewModel = koinViewModel(),
    onDriverClick: (driverId: String, driverName: String) -> Unit,
    onSignOut: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AdminHomeScreenContent(
        uiState = uiState,
        onAction = viewModel::onAction,
        onDriverClick = onDriverClick,
        onSignOut = onSignOut,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AdminHomeScreenContent(
    uiState: AdminHomeState,
    onAction: (AdminHomeAction) -> Unit,
    onDriverClick: (driverId: String, driverName: String) -> Unit,
    onSignOut: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(Res.string.admin_home_title),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.semantics { heading() },
                        )
                        Text(
                            text = stringResource(Res.string.admin_home_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSignOut) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = stringResource(Res.string.client_home_sign_out_desc),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = { onAction(AdminHomeAction.Refresh) },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when {
                uiState.isLoading && uiState.pendingDrivers.isEmpty() -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }

                uiState.error != null -> Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    ErrorCard(
                        message = stringResource(Res.string.admin_home_load_error),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                uiState.pendingDrivers.isEmpty() -> Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        ),
                    ) {
                        Text(
                            text = stringResource(Res.string.admin_home_empty),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(16.dp),
                ) {
                    items(
                        items = uiState.pendingDrivers,
                        key = { it.id },
                        contentType = { "pending_driver" }
                    ) { driver ->
                        PendingDriverCard(
                            driver = driver,
                            onClick = { onDriverClick(driver.id, driver.fullName) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PendingDriverCard(driver: User, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = driver.fullName.ifBlank { driver.email },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = driver.email,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            driver.vehicle?.let { vehicle ->
                Text(
                    text = stringResource(Res.string.admin_home_plate, vehicle.plate, vehicle.model),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
