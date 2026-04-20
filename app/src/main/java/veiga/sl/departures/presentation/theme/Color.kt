package veiga.sl.departures.presentation.theme

import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material3.ColorScheme

val SLBlue = Color(0xFF006EC1)
val SLDarkBlue = Color(0xFF005A9E)
val SLWhite = Color(0xFFFFFFFF)
val SLBlack = Color(0xFF000000)

val SLColorScheme =
    ColorScheme(
        primary = SLBlue,
        primaryDim = SLDarkBlue,
        primaryContainer = SLDarkBlue,
        onPrimary = SLWhite,
        onPrimaryContainer = SLWhite,
        secondary = SLBlue,
        secondaryDim = SLDarkBlue,
        secondaryContainer = SLDarkBlue,
        onSecondary = SLWhite,
        onSecondaryContainer = SLWhite,
        tertiary = SLBlue,
        tertiaryDim = SLDarkBlue,
        tertiaryContainer = SLDarkBlue,
        onTertiary = SLWhite,
        onTertiaryContainer = SLWhite,
        surfaceContainerLow = SLBlack,
        surfaceContainer = SLBlack,
        surfaceContainerHigh = SLBlack,
        onSurface = SLWhite,
        onSurfaceVariant = Color.Gray,
        outline = Color.LightGray,
        outlineVariant = Color.DarkGray,
        background = SLBlack,
        onBackground = SLWhite,
        error = Color.Red,
        onError = SLWhite,
        errorContainer = Color.Red,
        onErrorContainer = SLWhite,
    )
