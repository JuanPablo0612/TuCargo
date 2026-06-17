package com.juanpablo0612.tucargo.core.ui.theme

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass

enum class WindowType { COMPACT, MEDIUM, EXPANDED }

@Composable
fun rememberWindowType(): WindowType {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    return when {
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) -> WindowType.EXPANDED
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) -> WindowType.MEDIUM
        else -> WindowType.COMPACT
    }
}

@Immutable
data class Dimensions(
    val screenHorizontalPadding: Dp,
    val formHorizontalPadding: Dp,
    val contentMaxWidth: Dp,
    val mapHeight: Dp,
    val buttonHeight: Dp,
    val documentPickerHeight: Dp,
    val loadingPlaceholderHeight: Dp,
    val cardInternalPadding: Dp,
    val sectionSpacing: Dp,
    val iconSizeSmall: Dp,
    val iconSizeMedium: Dp,
    val iconSizeLarge: Dp,
    val avatarSize: Dp,
    val logoSize: Dp,
)

val CompactDimensions = Dimensions(
    screenHorizontalPadding = 16.dp,
    formHorizontalPadding = 24.dp,
    contentMaxWidth = Dp.Unspecified,
    mapHeight = 220.dp,
    buttonHeight = 56.dp,
    documentPickerHeight = 120.dp,
    loadingPlaceholderHeight = 100.dp,
    cardInternalPadding = 12.dp,
    sectionSpacing = 16.dp,
    iconSizeSmall = 24.dp,
    iconSizeMedium = 40.dp,
    iconSizeLarge = 48.dp,
    avatarSize = 36.dp,
    logoSize = 80.dp,
)

val MediumDimensions = Dimensions(
    screenHorizontalPadding = 24.dp,
    formHorizontalPadding = 32.dp,
    contentMaxWidth = 600.dp,
    mapHeight = 280.dp,
    buttonHeight = 56.dp,
    documentPickerHeight = 140.dp,
    loadingPlaceholderHeight = 120.dp,
    cardInternalPadding = 16.dp,
    sectionSpacing = 20.dp,
    iconSizeSmall = 24.dp,
    iconSizeMedium = 44.dp,
    iconSizeLarge = 56.dp,
    avatarSize = 40.dp,
    logoSize = 96.dp,
)

val ExpandedDimensions = Dimensions(
    screenHorizontalPadding = 32.dp,
    formHorizontalPadding = 32.dp,
    contentMaxWidth = 640.dp,
    mapHeight = 340.dp,
    buttonHeight = 56.dp,
    documentPickerHeight = 160.dp,
    loadingPlaceholderHeight = 120.dp,
    cardInternalPadding = 16.dp,
    sectionSpacing = 24.dp,
    iconSizeSmall = 28.dp,
    iconSizeMedium = 48.dp,
    iconSizeLarge = 64.dp,
    avatarSize = 44.dp,
    logoSize = 112.dp,
)

val LocalDimensions = compositionLocalOf { CompactDimensions }
