package com.trynite.chefyarnnn.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val BlushMintLightColorScheme = lightColorScheme(
    primary = RoseLightPrimary,
    onPrimary = RoseLightOnPrimary,
    primaryContainer = RoseLightPrimaryContainer,
    onPrimaryContainer = RoseLightOnPrimaryContainer,
    secondary = MintLightSecondary,
    onSecondary = MintLightOnSecondary,
    secondaryContainer = MintLightSecondaryContainer,
    onSecondaryContainer = MintLightOnSecondaryContainer,
    tertiary = ButterLightTertiary,
    onTertiary = ButterLightOnTertiary,
    tertiaryContainer = ButterLightTertiaryContainer,
    onTertiaryContainer = ButterLightOnTertiaryContainer,
    background = CreamBackground,
    onBackground = CreamOnBackground,
    surface = CreamSurface,
    onSurface = CreamOnSurface,
    surfaceVariant = WarmSurfaceVariant,
    onSurfaceVariant = WarmOnSurfaceVariant,
    outline = WarmOutline
)

private val BlushMintDarkColorScheme = darkColorScheme(
    primary = RoseDarkPrimary,
    onPrimary = RoseDarkOnPrimary,
    primaryContainer = RoseDarkPrimaryContainer,
    onPrimaryContainer = RoseDarkOnPrimaryContainer,
    secondary = MintDarkSecondary,
    onSecondary = MintDarkOnSecondary,
    secondaryContainer = MintDarkSecondaryContainer,
    onSecondaryContainer = MintDarkOnSecondaryContainer,
    tertiary = ButterDarkTertiary,
    onTertiary = ButterDarkOnTertiary,
    tertiaryContainer = ButterDarkTertiaryContainer,
    onTertiaryContainer = ButterDarkOnTertiaryContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline
)

private val SoftRoundedShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp)
)

@Composable
fun chefTextFieldColors(
    focusedBorderColor: Color = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
) = OutlinedTextFieldDefaults.colors(
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
    cursorColor = MaterialTheme.colorScheme.primary,
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    focusedBorderColor = focusedBorderColor,
    unfocusedBorderColor = unfocusedBorderColor
)

@Composable
fun ChefyarnnnTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> BlushMintDarkColorScheme
        else -> BlushMintLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = SoftRoundedShapes,
        content = content
    )
}
