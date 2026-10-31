package com.trynite.chefyarnnn.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val WarmFilipinoLightColorScheme = lightColorScheme(
    primary = TerracottaLightPrimary,
    onPrimary = TerracottaLightOnPrimary,
    primaryContainer = TerracottaLightPrimaryContainer,
    onPrimaryContainer = TerracottaLightOnPrimaryContainer,
    secondary = PandanLightSecondary,
    onSecondary = PandanLightOnSecondary,
    secondaryContainer = PandanLightSecondaryContainer,
    onSecondaryContainer = PandanLightOnSecondaryContainer,
    tertiary = CalamansiLightTertiary,
    onTertiary = CalamansiLightOnTertiary,
    tertiaryContainer = CalamansiLightTertiaryContainer,
    onTertiaryContainer = CalamansiLightOnTertiaryContainer,
    background = CreamBackground,
    onBackground = CreamOnBackground,
    surface = CreamSurface,
    onSurface = CreamOnSurface,
    surfaceVariant = WarmSurfaceVariant,
    onSurfaceVariant = WarmOnSurfaceVariant,
    outline = WarmOutline
)

private val WarmFilipinoDarkColorScheme = darkColorScheme(
    primary = TerracottaDarkPrimary,
    onPrimary = TerracottaDarkOnPrimary,
    primaryContainer = TerracottaDarkPrimaryContainer,
    onPrimaryContainer = TerracottaDarkOnPrimaryContainer,
    secondary = PandanDarkSecondary,
    onSecondary = PandanDarkOnSecondary,
    secondaryContainer = PandanDarkSecondaryContainer,
    onSecondaryContainer = PandanDarkOnSecondaryContainer,
    tertiary = CalamansiDarkTertiary,
    onTertiary = CalamansiDarkOnTertiary,
    tertiaryContainer = CalamansiDarkTertiaryContainer,
    onTertiaryContainer = CalamansiDarkOnTertiaryContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline
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
        darkTheme -> WarmFilipinoDarkColorScheme
        else -> WarmFilipinoLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
