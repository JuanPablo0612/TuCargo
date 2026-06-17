package com.juanpablo0612.tucargo.features.auth.documents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import org.jetbrains.compose.resources.painterResource
import tucargo.composeapp.generated.resources.info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.juanpablo0612.tucargo.core.ui.components.ResponsiveContainer
import com.juanpablo0612.tucargo.core.ui.theme.LocalDimensions
import com.juanpablo0612.tucargo.domain.model.KycDocument
import com.juanpablo0612.tucargo.domain.model.KycDocumentType
import com.juanpablo0612.tucargo.domain.model.KycStatus
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import tucargo.composeapp.generated.resources.Res
import tucargo.composeapp.generated.resources.kyc_pending_personal_section
import tucargo.composeapp.generated.resources.kyc_pending_rejection_reason
import tucargo.composeapp.generated.resources.kyc_pending_subtitle
import tucargo.composeapp.generated.resources.kyc_pending_title
import tucargo.composeapp.generated.resources.kyc_pending_vehicle_section
import tucargo.composeapp.generated.resources.kyc_status_approved
import tucargo.composeapp.generated.resources.kyc_status_pending
import tucargo.composeapp.generated.resources.kyc_status_rejected
import tucargo.composeapp.generated.resources.driver_docs_id_front
import tucargo.composeapp.generated.resources.driver_docs_id_back
import tucargo.composeapp.generated.resources.driver_docs_license
import tucargo.composeapp.generated.resources.driver_docs_soat
import tucargo.composeapp.generated.resources.driver_docs_tech_review
import tucargo.composeapp.generated.resources.driver_docs_registration_card

@Composable
fun KycPendingScreen(
    viewModel: KycPendingViewModel = koinViewModel(),
    onVerified: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isVerified) {
        if (uiState.isVerified) {
            onVerified()
        }
    }

    KycPendingScreenContent(uiState = uiState)
}

@Composable
internal fun KycPendingScreenContent(uiState: KycPendingState) {
    if (uiState.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val dimensions = LocalDimensions.current
    ResponsiveContainer(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = dimensions.formHorizontalPadding)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(40.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(80.dp)
                .clip(MaterialTheme.shapes.extraLarge)
                .background(MaterialTheme.colorScheme.secondaryContainer),
        ) {
            Icon(
                painter = painterResource(Res.drawable.info),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = stringResource(Res.string.kyc_pending_title),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = stringResource(Res.string.kyc_pending_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (uiState.documents.isNotEmpty()) {
            Spacer(Modifier.height(28.dp))

            val personalDocs = uiState.documents.filter { it.type in personalDocTypes }
            val vehicleDocs = uiState.documents.filter { it.type in vehicleDocTypes }

            if (personalDocs.isNotEmpty()) {
                DocumentSection(
                    title = stringResource(Res.string.kyc_pending_personal_section),
                    docs = personalDocs,
                )
            }

            if (vehicleDocs.isNotEmpty()) {
                Spacer(Modifier.height(20.dp))
                HorizontalDivider()
                Spacer(Modifier.height(20.dp))
                DocumentSection(
                    title = stringResource(Res.string.kyc_pending_vehicle_section),
                    docs = vehicleDocs,
                )
            }
        }

        Spacer(Modifier.height(32.dp))
    }
    }
}

@Composable
private fun DocumentSection(
    title: String,
    docs: List<KycDocument>,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(10.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            docs.forEach { doc ->
                KycDocumentStatusRow(doc = doc)
            }
        }
    }
}

@Composable
private fun KycDocumentStatusRow(doc: KycDocument) {
    val statusColor = when (doc.status) {
        KycStatus.APPROVED -> MaterialTheme.colorScheme.tertiary
        KycStatus.REJECTED -> MaterialTheme.colorScheme.error
        KycStatus.PENDING -> MaterialTheme.colorScheme.outline
    }
    val statusBgColor = when (doc.status) {
        KycStatus.APPROVED -> MaterialTheme.colorScheme.tertiaryContainer
        KycStatus.REJECTED -> MaterialTheme.colorScheme.errorContainer
        KycStatus.PENDING -> MaterialTheme.colorScheme.surfaceContainerHighest
    }
    val statusLabel = when (doc.status) {
        KycStatus.APPROVED -> stringResource(Res.string.kyc_status_approved)
        KycStatus.REJECTED -> stringResource(Res.string.kyc_status_rejected)
        KycStatus.PENDING -> stringResource(Res.string.kyc_status_pending)
    }
    val docLabel = when (doc.type) {
        KycDocumentType.ID_FRONT -> stringResource(Res.string.driver_docs_id_front)
        KycDocumentType.ID_BACK -> stringResource(Res.string.driver_docs_id_back)
        KycDocumentType.DRIVER_LICENSE -> stringResource(Res.string.driver_docs_license)
        KycDocumentType.SOAT -> stringResource(Res.string.driver_docs_soat)
        KycDocumentType.VEHICLE_TECH_REVIEW -> stringResource(Res.string.driver_docs_tech_review)
        KycDocumentType.VEHICLE_REGISTRATION_CARD -> stringResource(Res.string.driver_docs_registration_card)
    }

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = docLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = statusBgColor,
                    modifier = Modifier.padding(start = 8.dp),
                ) {
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }
            if (doc.status == KycStatus.REJECTED && doc.rejectionReason != null) {
                Text(
                    text = stringResource(Res.string.kyc_pending_rejection_reason, doc.rejectionReason),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

private val personalDocTypes = setOf(
    KycDocumentType.ID_FRONT,
    KycDocumentType.ID_BACK,
    KycDocumentType.DRIVER_LICENSE
)

private val vehicleDocTypes = setOf(
    KycDocumentType.SOAT,
    KycDocumentType.VEHICLE_TECH_REVIEW,
    KycDocumentType.VEHICLE_REGISTRATION_CARD
)
