package com.juanpablo0612.tucargo.features.admin.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import org.jetbrains.compose.resources.painterResource
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.juanpablo0612.tucargo.core.ui.components.ResponsiveContainer
import com.juanpablo0612.tucargo.core.ui.theme.LocalDimensions
import com.juanpablo0612.tucargo.core.ui.components.ErrorCard
import com.juanpablo0612.tucargo.core.ui.components.LoadingButton
import com.juanpablo0612.tucargo.core.ui.components.RoundedTextField
import com.juanpablo0612.tucargo.domain.model.KycDocument
import com.juanpablo0612.tucargo.domain.model.KycDocumentType
import com.juanpablo0612.tucargo.domain.model.KycStatus
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import tucargo.composeapp.generated.resources.Res
import tucargo.composeapp.generated.resources.admin_review_action_error
import tucargo.composeapp.generated.resources.arrow_back
import tucargo.composeapp.generated.resources.admin_review_approve
import tucargo.composeapp.generated.resources.admin_review_back_button
import tucargo.composeapp.generated.resources.admin_review_load_error
import tucargo.composeapp.generated.resources.admin_review_no_documents
import tucargo.composeapp.generated.resources.admin_review_reject
import tucargo.composeapp.generated.resources.admin_review_retry
import tucargo.composeapp.generated.resources.admin_review_reject_dialog_cancel
import tucargo.composeapp.generated.resources.admin_review_reject_dialog_confirm
import tucargo.composeapp.generated.resources.admin_review_reject_dialog_hint
import tucargo.composeapp.generated.resources.admin_review_reject_dialog_title
import tucargo.composeapp.generated.resources.admin_review_verify_button
import tucargo.composeapp.generated.resources.admin_review_verify_error
import tucargo.composeapp.generated.resources.admin_review_verify_hint
import tucargo.composeapp.generated.resources.admin_review_view_document
import tucargo.composeapp.generated.resources.driver_docs_id_back
import tucargo.composeapp.generated.resources.driver_docs_id_front
import tucargo.composeapp.generated.resources.driver_docs_license
import tucargo.composeapp.generated.resources.driver_docs_registration_card
import tucargo.composeapp.generated.resources.driver_docs_soat
import tucargo.composeapp.generated.resources.driver_docs_tech_review
import tucargo.composeapp.generated.resources.kyc_pending_personal_section
import tucargo.composeapp.generated.resources.kyc_pending_rejection_reason
import tucargo.composeapp.generated.resources.kyc_pending_vehicle_section
import tucargo.composeapp.generated.resources.kyc_status_approved
import tucargo.composeapp.generated.resources.kyc_status_pending
import tucargo.composeapp.generated.resources.kyc_status_rejected

@Composable
fun AdminDriverReviewScreen(
    driverId: String,
    driverName: String,
    onVerified: () -> Unit,
    onBackClick: () -> Unit,
) {
    val viewModel: AdminDriverReviewViewModel = koinViewModel { parametersOf(driverId) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isDriverVerified) {
        if (uiState.isDriverVerified) {
            onVerified()
            viewModel.onVerifiedNavigated()
        }
    }

    AdminDriverReviewScreenContent(
        uiState = uiState,
        driverName = driverName,
        onAction = viewModel::onAction,
        onBackClick = onBackClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AdminDriverReviewScreenContent(
    uiState: AdminDriverReviewState,
    driverName: String,
    onAction: (AdminDriverReviewAction) -> Unit,
    onBackClick: () -> Unit,
) {
    var rejectingType by remember { mutableStateOf<KycDocumentType?>(null) }

    rejectingType?.let { type ->
        val reasonState = rememberTextFieldState()
        AlertDialog(
            onDismissRequest = { rejectingType = null },
            title = { Text(stringResource(Res.string.admin_review_reject_dialog_title)) },
            text = {
                RoundedTextField(
                    state = reasonState,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(Res.string.admin_review_reject_dialog_hint)) },
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onAction(AdminDriverReviewAction.Reject(type, reasonState.text.toString()))
                        rejectingType = null
                    }
                ) {
                    Text(stringResource(Res.string.admin_review_reject_dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { rejectingType = null }) {
                    Text(stringResource(Res.string.admin_review_reject_dialog_cancel))
                }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = driverName,
                        modifier = Modifier.semantics { heading() },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(Res.drawable.arrow_back),
                            contentDescription = stringResource(Res.string.admin_review_back_button),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val dimensions = LocalDimensions.current
        ResponsiveContainer(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = dimensions.formHorizontalPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(dimensions.sectionSpacing),
        ) {
            Spacer(Modifier.height(0.dp))

            uiState.error?.let { error ->
                val msgRes = when (error) {
                    AdminDriverReviewError.LoadError -> Res.string.admin_review_load_error
                    AdminDriverReviewError.ReviewError -> Res.string.admin_review_action_error
                    AdminDriverReviewError.VerifyError -> Res.string.admin_review_verify_error
                }
                ErrorCard(message = stringResource(msgRes), modifier = Modifier.fillMaxWidth())
                if (error == AdminDriverReviewError.LoadError) {
                    OutlinedButton(
                        onClick = { onAction(AdminDriverReviewAction.Refresh) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(Res.string.admin_review_retry))
                    }
                }
            }

            if (uiState.documents.isEmpty() && uiState.error == null) {
                Text(
                    text = stringResource(Res.string.admin_review_no_documents),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else if (uiState.documents.isNotEmpty()) {
                val personalDocs = uiState.documents.filter { it.type in personalDocTypes }
                val vehicleDocs = uiState.documents.filter { it.type !in personalDocTypes }

                if (personalDocs.isNotEmpty()) {
                    ReviewSection(
                        title = stringResource(Res.string.kyc_pending_personal_section),
                        docs = personalDocs,
                        processingType = uiState.processingType,
                        onAction = onAction,
                        onRejectClick = { rejectingType = it },
                    )
                }
                if (vehicleDocs.isNotEmpty()) {
                    ReviewSection(
                        title = stringResource(Res.string.kyc_pending_vehicle_section),
                        docs = vehicleDocs,
                        processingType = uiState.processingType,
                        onAction = onAction,
                        onRejectClick = { rejectingType = it },
                    )
                }
            }

            Text(
                text = stringResource(Res.string.admin_review_verify_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            LoadingButton(
                onClick = { onAction(AdminDriverReviewAction.VerifyDriver) },
                isLoading = uiState.isVerifying,
                enabled = uiState.allDocumentsApproved,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(Res.string.admin_review_verify_button),
                    style = MaterialTheme.typography.titleSmall,
                )
            }

            Spacer(Modifier.height(16.dp))
        }
        }
    }
}

@Composable
private fun ReviewSection(
    title: String,
    docs: List<KycDocument>,
    processingType: KycDocumentType?,
    onAction: (AdminDriverReviewAction) -> Unit,
    onRejectClick: (KycDocumentType) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        docs.forEach { doc ->
            ReviewDocumentCard(
                doc = doc,
                isProcessing = processingType == doc.type,
                onApprove = { onAction(AdminDriverReviewAction.Approve(doc.type)) },
                onReject = { onRejectClick(doc.type) },
            )
        }
    }
}

@Composable
private fun ReviewDocumentCard(
    doc: KycDocument,
    isProcessing: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    val statusColor = when (doc.status) {
        KycStatus.APPROVED -> MaterialTheme.colorScheme.tertiary
        KycStatus.REJECTED -> MaterialTheme.colorScheme.error
        KycStatus.PENDING -> MaterialTheme.colorScheme.secondary
    }
    val statusLabel = when (doc.status) {
        KycStatus.APPROVED -> stringResource(Res.string.kyc_status_approved)
        KycStatus.REJECTED -> stringResource(Res.string.kyc_status_rejected)
        KycStatus.PENDING -> stringResource(Res.string.kyc_status_pending)
    }

    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = doc.type.displayLabel(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                Surface(
                    shape = MaterialTheme.shapes.extraSmall,
                    color = statusColor.copy(alpha = 0.15f),
                ) {
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TextButton(
                    onClick = { uriHandler.openUri(doc.imageUrl) },
                    enabled = doc.imageUrl.isNotBlank(),
                ) {
                    Text(stringResource(Res.string.admin_review_view_document))
                }
                Spacer(Modifier.weight(1f))
                if (doc.status == KycStatus.PENDING) {
                    OutlinedButton(onClick = onReject, enabled = !isProcessing) {
                        Text(
                            text = stringResource(Res.string.admin_review_reject),
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    LoadingButton(onClick = onApprove, isLoading = isProcessing) {
                        Text(stringResource(Res.string.admin_review_approve))
                    }
                }
            }
        }
    }
}

@Composable
private fun KycDocumentType.displayLabel(): String = when (this) {
    KycDocumentType.ID_FRONT -> stringResource(Res.string.driver_docs_id_front)
    KycDocumentType.ID_BACK -> stringResource(Res.string.driver_docs_id_back)
    KycDocumentType.DRIVER_LICENSE -> stringResource(Res.string.driver_docs_license)
    KycDocumentType.SOAT -> stringResource(Res.string.driver_docs_soat)
    KycDocumentType.VEHICLE_TECH_REVIEW -> stringResource(Res.string.driver_docs_tech_review)
    KycDocumentType.VEHICLE_REGISTRATION_CARD -> stringResource(Res.string.driver_docs_registration_card)
}

private val personalDocTypes = setOf(
    KycDocumentType.ID_FRONT,
    KycDocumentType.ID_BACK,
    KycDocumentType.DRIVER_LICENSE
)
