package com.juanpablo0612.tucargo.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import org.jetbrains.compose.resources.painterResource
import tucargo.composeapp.generated.resources.Res
import tucargo.composeapp.generated.resources.check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

enum class StepState { COMPLETED, ACTIVE, UPCOMING }

data class OnboardingStep(val label: String, val state: StepState)

@Composable
fun StepIndicator(
    steps: List<OnboardingStep>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.Top,
    ) {
        steps.forEachIndexed { index, step ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (index > 0) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(2.dp)
                                .background(
                                    if (step.state == StepState.UPCOMING)
                                        MaterialTheme.colorScheme.outlineVariant
                                    else
                                        MaterialTheme.colorScheme.primary
                                )
                        )
                    }

                    StepCircle(
                        stepNumber = index + 1,
                        state = step.state,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )

                    if (index < steps.lastIndex) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(2.dp)
                                .background(
                                    if (steps[index + 1].state == StepState.UPCOMING)
                                        MaterialTheme.colorScheme.outlineVariant
                                    else
                                        MaterialTheme.colorScheme.primary
                                )
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    text = step.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = when (step.state) {
                        StepState.ACTIVE -> MaterialTheme.colorScheme.primary
                        StepState.COMPLETED -> MaterialTheme.colorScheme.primary
                        StepState.UPCOMING -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun StepCircle(
    stepNumber: Int,
    state: StepState,
    modifier: Modifier = Modifier
) {
    val size = 28.dp
    when (state) {
        StepState.COMPLETED -> Box(
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(Res.drawable.check),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(16.dp)
            )
        }

        StepState.ACTIVE -> Box(
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        StepState.UPCOMING -> Box(
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

fun buildOnboardingSteps(
    currentStep: Int,
    labels: List<String>
): List<OnboardingStep> = labels.mapIndexed { index, label ->
    val stepNumber = index + 1
    OnboardingStep(
        label = label,
        state = when {
            stepNumber < currentStep -> StepState.COMPLETED
            stepNumber == currentStep -> StepState.ACTIVE
            else -> StepState.UPCOMING
        }
    )
}
