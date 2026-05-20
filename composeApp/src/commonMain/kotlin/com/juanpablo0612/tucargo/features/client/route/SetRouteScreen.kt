package com.juanpablo0612.tucargo.features.client.route

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.juanpablo0612.tucargo.core.ui.theme.BackgroundGray
import com.juanpablo0612.tucargo.core.ui.theme.PrimaryBlue
import com.juanpablo0612.tucargo.core.ui.theme.TextDarkGray
import com.juanpablo0612.tucargo.core.ui.theme.TextLightGray
import org.jetbrains.compose.resources.stringResource
import tucargo.composeapp.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetRouteScreen(
    onBackClick: () -> Unit = {},
    onChooseOnMapClick: () -> Unit = {},
    onSuggestionClick: (String) -> Unit = {}
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.route_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = BackgroundGray
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tarjeta Superior de Ruta
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Pick-up (Origen)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Indicador circular azul
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(PrimaryBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF3F4F6))
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = stringResource(Res.string.route_pickup),
                                    color = PrimaryBlue,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Default.MyLocation,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Línea vertical punteada
                    Box(
                        modifier = Modifier
                            .padding(start = 5.dp)
                            .width(2.dp)
                            .height(24.dp)
                            .background(Color.LightGray.copy(alpha = 0.5f)) // Simplificado para KMP
                    )

                    // Drop-off (Destino)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Indicador rombo negro (cuadrado rotado)
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.Black)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        TextField(
                            value = "",
                            onValueChange = {},
                            placeholder = {
                                Text(
                                    text = stringResource(Res.string.route_destination_hint),
                                    color = TextLightGray
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            singleLine = true
                        )
                    }
                }
            }

            // Acción Intermedia: Choose on map
            TextButton(
                onClick = onChooseOnMapClick,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Map,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(Res.string.route_choose_on_map),
                    color = PrimaryBlue,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sección SUGGESTIONS
            Text(
                text = stringResource(Res.string.route_suggestions),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextDarkGray,
                    letterSpacing = 1.sp
                )
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    SuggestionItem(
                        title = stringResource(Res.string.home_home),
                        address = "123 Main St, Springfield",
                        icon = Icons.Default.Home,
                        iconBgColor = Color(0xFFEBF5FF),
                        iconTint = PrimaryBlue,
                        onClick = { onSuggestionClick("Home") }
                    )
                }
                item {
                    SuggestionItem(
                        title = stringResource(Res.string.route_office),
                        address = "456 Business Rd, Metropolis",
                        icon = Icons.Default.Business,
                        iconBgColor = Color(0xFFFFF7ED),
                        iconTint = Color(0xFFF97316),
                        onClick = { onSuggestionClick("Office") }
                    )
                }
                item {
                    SuggestionItem(
                        title = "Starbucks Coffee",
                        address = "789 Caffeine Ave",
                        icon = Icons.Default.Coffee,
                        iconBgColor = Color(0xFFF3F4F6),
                        iconTint = Color(0xFF4B5563),
                        onClick = { onSuggestionClick("Starbucks") }
                    )
                }
                item {
                    SuggestionItem(
                        title = "Central Station",
                        address = "000 Transit Hub",
                        icon = Icons.Default.Train,
                        iconBgColor = Color(0xFFF3F4F6),
                        iconTint = Color(0xFF4B5563),
                        onClick = { onSuggestionClick("Central Station") }
                    )
                }
            }
        }
    }
}

@Composable
private fun SuggestionItem(
    title: String,
    address: String,
    icon: ImageVector,
    iconBgColor: Color,
    iconTint: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                )
                Text(
                    text = address,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextDarkGray
                    )
                )
            }
            Icon(
                imageVector = Icons.Default.ArrowOutward,
                contentDescription = null,
                tint = TextLightGray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
